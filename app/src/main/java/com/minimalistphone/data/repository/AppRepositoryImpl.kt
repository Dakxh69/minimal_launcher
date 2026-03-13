package com.minimalistphone.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.os.Process
import com.minimalistphone.data.database.dao.BlockedAppDao
import com.minimalistphone.domain.models.AppInfo
import com.minimalistphone.domain.repository.AppRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AppRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val blockedAppDao: BlockedAppDao
) : AppRepository {

    private var cachedApps: List<AppInfo>? = null
    
    override suspend fun getInstalledApps(): List<AppInfo> {
        cachedApps?.let { return it }
        return withContext(Dispatchers.IO) {
            val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
            val profiles = launcherApps.profiles
            val apps = ArrayList<AppInfo>(100)

            for (profile in profiles) {
                val activityList = launcherApps.getActivityList(null, profile)
                for (activity in activityList) {
                    apps.add(
                        AppInfo(
                            packageName = activity.applicationInfo.packageName,
                            label = activity.label.toString()
                        )
                    )
                }
            }
            val sorted = apps.sortedBy { it.label }
            cachedApps = sorted
            sorted
        }
    }

    override fun launchApp(packageName: String) {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION)
            context.startActivity(launchIntent)
        }
    }
}
