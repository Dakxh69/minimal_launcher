package com.minimalistphone.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_usage")
data class UsageEntity(
    @PrimaryKey val packageName: String,
    val totalTimeInForegroundMs: Long,
    val unlockCount: Int,
    val lastUpdated: Long
)
