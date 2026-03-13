package com.minimalistphone.presentation.delay

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimalistphone.ui.theme.MinimalistGrey
import com.minimalistphone.ui.theme.MutedGrey
import com.minimalistphone.ui.theme.TrueBlack
import kotlinx.coroutines.delay

@Composable
fun AppDelayScreen(
    appName: String,
    onCancel: () -> Unit,
    onContinue: () -> Unit
) {
    var timeLeft by remember { mutableIntStateOf(5) }

    LaunchedEffect(Unit) {
        while (timeLeft > 0) {
            delay(1000)
            timeLeft--
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TrueBlack)
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Pause for a moment",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-1).sp,
                color = MinimalistGrey,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "You are about to open $appName.",
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal,
                color = MinimalistGrey,
                textAlign = TextAlign.Center
            )
        }

        Column(
            modifier = Modifier.padding(bottom = 64.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(192.dp)
                    .border(2.dp, Color(0xFF334155), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "0$timeLeft",
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-2).sp,
                    color = MinimalistGrey
                )
            }
            
            Text(
                text = "SECONDS",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp,
                color = MinimalistGrey,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        Column(
            modifier = Modifier.width(320.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1A1A1A))
                    .clickable(enabled = timeLeft == 0) { onContinue() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Open Anyway",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (timeLeft == 0) MinimalistGrey else MutedGrey
                )
            }
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1E293B))
                    .clickable { onCancel() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Cancel",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MinimalistGrey
                )
            }
        }
    }
}
