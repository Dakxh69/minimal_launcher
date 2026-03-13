package com.minimalistphone.services

import android.app.usage.UsageStatsManager
import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.minimalistphone.data.database.dao.UsageDao
import com.minimalistphone.data.models.UsageEntity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class UsageTrackingWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val usageDao: UsageDao
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val usageStatsManager = applicationContext.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val endTime = System.currentTimeMillis()
        val startTime = endTime - TimeUnit.DAYS.toMillis(1)

        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY, startTime, endTime
        )

        stats?.forEach { stat ->
            usageDao.insertUsage(
                UsageEntity(
                    packageName = stat.packageName,
                    totalTimeInForegroundMs = stat.totalTimeInForeground,
                    unlockCount = 0, 
                    lastUpdated = endTime
                )
            )
        }
        return Result.success()
    }
}
