package com.minimalistphone.presentation.focusmode

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimalistphone.ui.theme.MinimalistGrey
import com.minimalistphone.ui.theme.TrueBlack
import kotlinx.coroutines.delay

@Composable
fun FocusModeScreen(
    onEndSession: () -> Unit
) {
    // Duration selection or active timer
    var sessionStarted by remember { mutableStateOf(false) }
    var selectedMinutes by remember { mutableIntStateOf(25) }
    var remainingSeconds by remember { mutableIntStateOf(0) }

    // Countdown timer
    LaunchedEffect(sessionStarted) {
        if (sessionStarted) {
            remainingSeconds = selectedMinutes * 60
            while (remainingSeconds > 0) {
                delay(1000L)
                remainingSeconds--
            }
            // Session complete
            sessionStarted = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TrueBlack)
            .statusBarsPadding()
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Text(
            text = "FOCUS SESSION",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 3.sp,
            color = MinimalistGrey.copy(alpha = 0.5f),
            modifier = Modifier.padding(top = 48.dp)
        )

        if (!sessionStarted) {
            // Duration picker
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                Text(
                    text = "Select Duration",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Light,
                    color = MinimalistGrey
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    listOf(15, 25, 45, 60).forEach { mins ->
                        val isSelected = selectedMinutes == mins
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0xFF334155) else Color(0xFF1E293B))
                                .clickable { selectedMinutes = mins }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${mins}m",
                                fontSize = 16.sp,
                                color = if (isSelected) MinimalistGrey else MinimalistGrey.copy(alpha = 0.4f)
                            )
                        }
                    }
                }

                // Start button
                Box(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF334155))
                        .clickable { sessionStarted = true }
                        .padding(horizontal = 48.dp, vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "START",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 3.sp,
                        color = MinimalistGrey
                    )
                }
            }
        } else {
            // Active timer
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val mins = remainingSeconds / 60
                val secs = remainingSeconds % 60
                Text(
                    text = "%02d:%02d".format(mins, secs),
                    fontSize = 80.sp,
                    fontWeight = FontWeight.Light,
                    color = MinimalistGrey,
                    letterSpacing = (-3).sp
                )

                Text(
                    text = "Focus on what matters.",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Light,
                    color = MinimalistGrey.copy(alpha = 0.5f)
                )

                // Progress bar
                val progress = if (selectedMinutes > 0) {
                    1f - (remainingSeconds.toFloat() / (selectedMinutes * 60))
                } else 0f

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .padding(top = 16.dp)
                        .height(2.dp)
                        .background(Color(0xFF1E293B)),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .height(2.dp)
                            .background(MinimalistGrey.copy(alpha = 0.5f))
                    )
                }
            }
        }

        // Footer
        Column(
            modifier = Modifier.padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (sessionStarted) {
                Text(
                    text = "END SESSION",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 2.sp,
                    color = MinimalistGrey.copy(alpha = 0.4f),
                    modifier = Modifier
                        .clickable {
                            sessionStarted = false
                            onEndSession()
                        }
                        .padding(16.dp)
                )
            } else {
                Text(
                    text = "← Back",
                    fontSize = 12.sp,
                    color = MinimalistGrey.copy(alpha = 0.4f),
                    modifier = Modifier
                        .clickable { onEndSession() }
                        .padding(16.dp)
                )
            }
        }
    }
}
