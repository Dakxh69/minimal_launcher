package com.minimalistphone.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimalistphone.domain.models.AppInfo
import com.minimalistphone.ui.theme.MinimalistGrey
import com.minimalistphone.ui.theme.TrueBlack

@Composable
fun SettingsScreen(
    allApps: List<AppInfo>,
    pinnedPackages: Set<String>,
    hiddenPackages: Set<String>,
    blockedPackages: Set<String>,
    notifControl: String,
    grayscaleMode: Boolean,
    mindfulnessPrompts: Boolean,
    onTogglePinned: (String) -> Unit,
    onToggleHidden: (String) -> Unit,
    onToggleBlocked: (String) -> Unit,
    onNavigateFocusSession: () -> Unit,
    onNavigateScreenTime: () -> Unit,
    onCycleNotifControl: () -> Unit,
    onToggleGrayscale: () -> Unit,
    onToggleMindfulness: () -> Unit,
    onGoBack: () -> Unit
) {
    var currentSubScreen by remember { mutableStateOf<SettingsSubScreen?>(null) }

    when (currentSubScreen) {
        SettingsSubScreen.PINNED_APPS -> {
            AppListEditor(
                title = "Pinned Apps",
                subtitle = "Choose apps to show on home screen",
                allApps = allApps,
                selectedPackages = pinnedPackages,
                onToggle = onTogglePinned,
                onGoBack = { currentSubScreen = null }
            )
        }
        SettingsSubScreen.BLOCKED_APPS -> {
            AppListEditor(
                title = "Blocked Apps",
                subtitle = "These apps cannot be opened",
                allApps = allApps,
                selectedPackages = blockedPackages,
                onToggle = onToggleBlocked,
                onGoBack = { currentSubScreen = null }
            )
        }
        SettingsSubScreen.HIDDEN_APPS -> {
            AppListEditor(
                title = "Hidden Apps",
                subtitle = "Hidden from search list",
                allApps = allApps,
                selectedPackages = hiddenPackages,
                onToggle = onToggleHidden,
                onGoBack = { currentSubScreen = null }
            )
        }
        null -> {
            // Main settings menu
            var totalDrag by remember { mutableFloatStateOf(0f) }
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
                            if (totalDrag > 150) onGoBack()
                            totalDrag = 0f
                        }
                    )
            ) {
                // Header
                Column(modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 48.dp, bottom = 32.dp)) {
                    Text(
                        text = "Settings",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Light,
                        letterSpacing = (-1).sp,
                        color = MinimalistGrey
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    SettingsMenuItem(
                        title = "Pinned Apps",
                        subtitle = "${pinnedPackages.size} apps pinned",
                        onClick = { currentSubScreen = SettingsSubScreen.PINNED_APPS }
                    )
                    SettingsMenuItem(
                        title = "Blocked Apps",
                        subtitle = "${blockedPackages.size} apps blocked",
                        onClick = { currentSubScreen = SettingsSubScreen.BLOCKED_APPS }
                    )
                    SettingsMenuItem(
                        title = "Hidden Apps",
                        subtitle = "${hiddenPackages.size} apps hidden",
                        onClick = { currentSubScreen = SettingsSubScreen.HIDDEN_APPS }
                    )
                    SettingsMenuItem(
                        title = "Focus Session",
                        subtitle = "Start a distraction-free session",
                        onClick = { onNavigateFocusSession() }
                    )
                    SettingsMenuItem(
                        title = "Screen Time",
                        subtitle = "View usage stats and reports",
                        onClick = { onNavigateScreenTime() }
                    )
                    SettingsMenuItem(
                        title = "Notification Control",
                        subtitle = notifControl.uppercase(),
                        onClick = { onCycleNotifControl() }
                    )
                    SettingsMenuItem(
                        title = "Grayscale Mode",
                        subtitle = if (grayscaleMode) "ON" else "OFF",
                        onClick = { onToggleGrayscale() }
                    )
                    SettingsMenuItem(
                        title = "Mindfulness Prompts",
                        subtitle = if (mindfulnessPrompts) "ON" else "OFF",
                        onClick = { onToggleMindfulness() }
                    )
                }
            }
        }
    }
}

private enum class SettingsSubScreen {
    PINNED_APPS, BLOCKED_APPS, HIDDEN_APPS
}

@Composable
private fun SettingsMenuItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 24.dp)
    ) {
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Light,
            color = MinimalistGrey
        )
        Text(
            text = subtitle,
            fontSize = 11.sp,
            color = MinimalistGrey.copy(alpha = 0.4f),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color(0xFF1E293B))
    )
}

@Composable
private fun AppListEditor(
    title: String,
    subtitle: String,
    allApps: List<AppInfo>,
    selectedPackages: Set<String>,
    onToggle: (String) -> Unit,
    onGoBack: () -> Unit
) {
    val sortedApps = remember(allApps) { allApps.sortedBy { it.label.uppercase() } }
    var totalDrag by remember { mutableFloatStateOf(0f) }

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
                    if (totalDrag > 150) onGoBack()
                    totalDrag = 0f
                }
            )
    ) {
        // Header
        Column(modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 48.dp, bottom = 16.dp)) {
            Text(
                text = title,
                fontSize = 28.sp,
                fontWeight = FontWeight.Light,
                color = MinimalistGrey
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = MinimalistGrey.copy(alpha = 0.4f),
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFF1E293B))
            )
        }

        // App list with toggle indicators
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            items(
                items = sortedApps,
                key = { it.packageName }
            ) { app ->
                val isSelected = app.packageName in selectedPackages
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToggle(app.packageName) }
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = app.label,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Light,
                        color = if (isSelected) MinimalistGrey else MinimalistGrey.copy(alpha = 0.3f)
                    )
                    Text(
                        text = if (isSelected) "●" else "○",
                        fontSize = 14.sp,
                        color = if (isSelected) MinimalistGrey else MinimalistGrey.copy(alpha = 0.2f)
                    )
                }
            }
        }
    }
}