package com.minimalistphone.di

import android.content.Context
import androidx.room.Room
import com.minimalistphone.data.database.MinimalistDatabase
import com.minimalistphone.data.database.dao.BlockedAppDao
import com.minimalistphone.data.database.dao.UsageDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MinimalistDatabase {
        return Room.databaseBuilder(
            context,
            MinimalistDatabase::class.java,
            "minimalist_database"
        ).build()
    }

    @Provides
    fun provideUsageDao(database: MinimalistDatabase): UsageDao {
        return database.usageDao()
    }

    @Provides
    fun provideBlockedAppDao(database: MinimalistDatabase): BlockedAppDao {
        return database.blockedAppDao()
    }
}
