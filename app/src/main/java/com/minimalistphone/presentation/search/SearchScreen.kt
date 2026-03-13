package com.minimalistphone.presentation.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimalistphone.domain.models.AppInfo
import com.minimalistphone.ui.theme.MinimalistGrey
import com.minimalistphone.ui.theme.MutedGrey
import com.minimalistphone.ui.theme.TrueBlack
import kotlinx.coroutines.launch

@Composable
fun SearchScreen(
    allApps: List<AppInfo>,
    onAppClick: (String) -> Unit,
    onGoBack: () -> Unit,
    onNavigateSettings: () -> Unit,
    onHideApp: (String) -> Unit,
    onBlockApp: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var totalDrag by remember { mutableFloatStateOf(0f) }
    var swiped by remember { mutableStateOf(false) }

    LaunchedEffect(swiped) {
        if (swiped) {
            kotlinx.coroutines.delay(500)
            swiped = false
        }
    }

    val sortedApps = remember(allApps) { allApps.sortedBy { it.label.uppercase() } }

    val filteredApps = remember(sortedApps, searchQuery) {
        if (searchQuery.isEmpty()) sortedApps
        else {
            val query = searchQuery.lowercase()
            sortedApps.mapNotNull { app ->
                val label = app.label.lowercase()
                val score = when {
                    label == query -> 3
                    label.startsWith(query) -> 2
                    label.contains(query) -> 1
                    fuzzyMatch(query, label) -> 0
                    else -> -1
                }
                if (score >= 0) app to score else null
            }.sortedByDescending { it.second }.map { it.first }
        }
    }

    val alphabet = remember(sortedApps) {
        sortedApps.mapNotNull { it.label.firstOrNull()?.uppercaseChar() }
            .distinct()
            .filter { it.isLetter() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TrueBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
            .draggable(
                orientation = Orientation.Horizontal,
                state = rememberDraggableState { delta ->
                    totalDrag += delta
                },
                onDragStopped = {
                    if (totalDrag > 150 && !swiped) {
                        swiped = true
                        onGoBack()
                    }
                    totalDrag = 0f
                }
            )
    ) {
        // Search bar
        Column(modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 48.dp, bottom = 16.dp)) {
            BasicTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                textStyle = TextStyle(
                    color = MinimalistGrey,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Light
                ),
                cursorBrush = SolidColor(MinimalistGrey),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                decorationBox = { innerTextField ->
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (searchQuery.isEmpty()) {
                            Text("Search apps...", color = MutedGrey, fontSize = 24.sp, fontWeight = FontWeight.Light)
                        }
                        innerTextField()
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .height(1.dp)
                    .background(Color(0xFF1E293B))
            )
        }

        // App list + alphabet sidebar
        Row(modifier = Modifier.weight(1f)) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 24.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                items(
                    items = filteredApps,
                    key = { it.packageName }
                ) { app ->
                    Text(
                        text = app.label,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Light,
                        color = MinimalistGrey,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAppClick(app.packageName) }
                    )
                }
            }

            // Alphabet sidebar
            if (searchQuery.isEmpty() && alphabet.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(end = 4.dp, top = 8.dp, bottom = 8.dp),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    alphabet.forEach { letter ->
                        Text(
                            text = letter.toString(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            color = MinimalistGrey.copy(alpha = 0.5f),
                            modifier = Modifier
                                .clickable {
                                    val index = filteredApps.indexOfFirst {
                                        it.label.uppercase().startsWith(letter)
                                    }
                                    if (index >= 0) {
                                        scope.launch { listState.animateScrollToItem(index) }
                                    }
                                }
                                .padding(vertical = 1.dp, horizontal = 8.dp)
                        )
                    }
                }
            }
        }

        // Settings icon at bottom right
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(bottom = 16.dp, end = 16.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            IconButton(onClick = { onNavigateSettings() }) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = MinimalistGrey.copy(alpha = 0.5f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

private fun fuzzyMatch(query: String, target: String): Boolean {
    var qi = 0
    for (ch in target) {
        if (qi < query.length && ch == query[qi]) qi++
    }
    return qi == query.length
}
