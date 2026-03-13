package com.minimalistphone.domain.repository

import com.minimalistphone.domain.models.AppInfo

interface AppRepository {
    suspend fun getInstalledApps(): List<AppInfo>
    fun launchApp(packageName: String)
}
