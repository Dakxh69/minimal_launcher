package com.minimalistphone.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocked_apps")
data class BlockedAppEntity(
    @PrimaryKey val packageName: String,
    val isBlockedDuringFocus: Boolean,
    val requiresDelayToOpen: Boolean
)
