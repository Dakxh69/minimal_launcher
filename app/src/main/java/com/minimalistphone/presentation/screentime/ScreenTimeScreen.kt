package com.minimalistphone.presentation.screentime

import android.app.usage.UsageStatsManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimalistphone.ui.theme.MinimalistGrey
import com.minimalistphone.ui.theme.TrueBlack
import java.util.Calendar
import java.util.concurrent.TimeUnit

data class AppUsageInfo(val name: String, val timeMs: Long, val fraction: Float)

private fun formatDuration(ms: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(ms)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(ms) % 60
    return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
}

@Composable
fun ScreenTimeScreen(
    onGoBack: () -> Unit,
    onNavigateSettings: () -> Unit,
    onNavigateFocus: () -> Unit
) {
    val context = LocalContext.current
    val usageStatsManager = remember {
        context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
    }

    // Today's usage
    val todayStats = remember(usageStatsManager) {
        if (usageStatsManager == null) return@remember emptyList<AppUsageInfo>()
        val endTime = System.currentTimeMillis()
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        val startTime = cal.timeInMillis
        val stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)
        val filtered = stats?.filter { it.totalTimeInForeground > 0 }
            ?.sortedByDescending { it.totalTimeInForeground }
            ?.take(10)
            ?: emptyList()
        val maxTime = filtered.maxOfOrNull { it.totalTimeInForeground }?.toFloat() ?: 1f
        filtered.map { stat ->
            val label = try {
                context.packageManager.getApplicationLabel(
                    context.packageManager.getApplicationInfo(stat.packageName, 0)
                ).toString()
            } catch (_: Exception) { stat.packageName.split(".").last() }
            AppUsageInfo(label, stat.totalTimeInForeground, (stat.totalTimeInForeground / maxTime).coerceIn(0.05f, 1f))
        }
    }

    val totalTodayMs = remember(todayStats) { todayStats.sumOf { it.timeMs } }

    // Weekly data (last 7 days)
    val weeklyData = remember(usageStatsManager) {
        if (usageStatsManager == null) return@remember List(7) { "?" to 0f }
        val days = listOf("M", "T", "W", "T", "F", "S", "S")
        val cal = Calendar.getInstance()
        val todayDow = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7 // Mon=0
        val result = mutableListOf<Pair<String, Long>>()
        for (i in 6 downTo 0) {
            val dayCal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -i)
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0)
            }
            val dayStart = dayCal.timeInMillis
            val dayEnd = dayStart + TimeUnit.DAYS.toMillis(1)
            val dayStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, dayStart, dayEnd)
            val total = dayStats?.sumOf { it.totalTimeInForeground } ?: 0L
            val dayIndex = (todayDow - i + 7) % 7
            result.add(days[dayIndex] to total)
        }
        val maxMs = result.maxOfOrNull { it.second }?.toFloat()?.coerceAtLeast(1f) ?: 1f
        result.map { (day, ms) -> day to (ms / maxMs).coerceIn(0f, 1f) }
    }

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
        Column(modifier = Modifier.padding(start = 32.dp, end = 32.dp, top = 48.dp)) {
            Text("CURRENT USAGE", fontSize = 10.sp, fontWeight = FontWeight.Medium, letterSpacing = 2.sp, color = MinimalistGrey, modifier = Modifier.padding(bottom = 8.dp))
            Text(
                text = formatDuration(totalTodayMs),
                fontSize = 48.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = (-2).sp,
                color = MinimalistGrey
            )
        }

        // Weekly Activity Chart
        Column(modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp)) {
            Text("WEEKLY ACTIVITY", fontSize = 10.sp, fontWeight = FontWeight.Medium, letterSpacing = 2.sp, color = MinimalistGrey, modifier = Modifier.padding(bottom = 32.dp))
            Row(
                modifier = Modifier.fillMaxWidth().height(128.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                weeklyData.forEachIndexed { index, (day, fraction) ->
                    val isToday = index == weeklyData.lastIndex
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Box(modifier = Modifier.fillMaxWidth().height((128 * fraction.coerceAtLeast(0.02f)).dp).background(if (isToday) MinimalistGrey else Color(0xFF1E293B)))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(day, fontSize = 10.sp, fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal, color = MinimalistGrey)
                    }
                }
            }
        }

        // Top Applications
        LazyColumn(modifier = Modifier.weight(1f).padding(horizontal = 32.dp, vertical = 16.dp)) {
            item {
                Text("TOP APPLICATIONS", fontSize = 10.sp, fontWeight = FontWeight.Medium, letterSpacing = 2.sp, color = MinimalistGrey, modifier = Modifier.padding(bottom = 32.dp))
            }
            if (todayStats.isEmpty()) {
                item {
                    Text("No usage data available.\nGrant Usage Access in Settings → Apps → Special Access → Usage Access.", fontSize = 14.sp, color = MinimalistGrey.copy(alpha = 0.5f))
                }
            }
            items(todayStats) { app ->
                Column(modifier = Modifier.padding(bottom = 40.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                        Text(app.name, fontSize = 16.sp, color = MinimalistGrey)
                        Text(formatDuration(app.timeMs), fontSize = 14.sp, color = MinimalistGrey)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF1E293B))) {
                        Box(modifier = Modifier.fillMaxWidth(app.fraction).height(1.dp).background(MinimalistGrey))
                    }
                }
            }
        }

        // Swipe hint
        Text(
            "← Swipe right to go back",
            fontSize = 10.sp,
            color = MinimalistGrey.copy(alpha = 0.3f),
            modifier = Modifier.padding(start = 32.dp, bottom = 16.dp)
        )
    }
}