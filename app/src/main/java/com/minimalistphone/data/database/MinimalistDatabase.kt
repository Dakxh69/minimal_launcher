package com.minimalistphone.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.minimalistphone.data.database.dao.BlockedAppDao
import com.minimalistphone.data.database.dao.UsageDao
import com.minimalistphone.data.models.BlockedAppEntity
import com.minimalistphone.data.models.UsageEntity

@Database(entities = [UsageEntity::class, BlockedAppEntity::class], version = 1, exportSchema = false)
abstract class MinimalistDatabase : RoomDatabase() {
    abstract fun usageDao(): UsageDao
    abstract fun blockedAppDao(): BlockedAppDao
}
