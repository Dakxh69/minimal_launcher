package com.minimalistphone.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimalistphone.domain.models.AppInfo
import com.minimalistphone.ui.theme.MinimalistGrey
import com.minimalistphone.ui.theme.TrueBlack
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    pinnedApps: List<AppInfo>,
    onAppClick: (String) -> Unit,
    onSwipeLeft: () -> Unit,
    showMindfulness: Boolean = false
) {
    var currentTime by remember { mutableStateOf(getFormattedTime()) }
    var currentDate by remember { mutableStateOf(getFormattedDate()) }
    var swiped by remember { mutableStateOf(false) }

    val mindfulnessPrompts = remember {
        listOf(
            "Why did you unlock your phone?",
            "What do you need to do right now?",
            "Is this worth your time?",
            "Take a deep breath first.",
            "Do you really need to check?",
            "Stay present. Stay focused."
        )
    }
    // New key each time screen is composed so prompt shows every time
    val promptKey = remember { System.currentTimeMillis() }
    var showPrompt by remember(promptKey) { mutableStateOf(showMindfulness) }
    val randomPrompt = remember(promptKey) { mindfulnessPrompts.random() }

    LaunchedEffect(Unit) {
        while (true) {
            delay(10000)
            currentTime = getFormattedTime()
            currentDate = getFormattedDate()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TrueBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, dragAmount ->
                    if (dragAmount < -80 && !swiped) {
                        swiped = true
                        onSwipeLeft()
                    }
                }
            }
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Time
            Text(
                text = currentTime,
                fontSize = 72.sp,
                fontWeight = FontWeight.Light,
                color = MinimalistGrey,
                letterSpacing = (-3).sp,
                modifier = Modifier.padding(start = 32.dp)
            )

            // Date
            Text(
                text = currentDate.uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MinimalistGrey.copy(alpha = 0.4f),
                letterSpacing = 3.sp,
                modifier = Modifier.padding(start = 32.dp, top = 8.dp)
            )

            Spacer(modifier = Modifier.height(64.dp))

            // Pinned Apps
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 32.dp)
            ) {
                item {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(28.dp)
                    ) {
                        pinnedApps.forEach { app ->
                            Text(
                                text = app.label,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Light,
                                color = MinimalistGrey,
                                modifier = Modifier
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) { onAppClick(app.packageName) }
                            )
                        }
                    }
                }
            }
        }

        // Swipe hint at bottom
        Text(
            text = "dakxh69",
            fontSize = 10.sp,
            color = MinimalistGrey.copy(alpha = 0.2f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )

        // Mindfulness prompt overlay
        if (showPrompt) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(TrueBlack.copy(alpha = 0.95f))
                    .clickable { showPrompt = false },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = randomPrompt,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Light,
                    color = MinimalistGrey,
                    modifier = Modifier.padding(horizontal = 48.dp)
                )
            }
        }
    }

    // Reset swipe state when recomposed
    LaunchedEffect(swiped) {
        if (swiped) {
            delay(500)
            swiped = false
        }
    }
}

private fun getFormattedTime(): String {
    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
}

private fun getFormattedDate(): String {
    return SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date())
}
