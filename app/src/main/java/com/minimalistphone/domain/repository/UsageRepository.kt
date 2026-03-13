package com.minimalistphone.domain.repository

import com.minimalistphone.data.models.UsageEntity
import kotlinx.coroutines.flow.Flow

interface UsageRepository {
    fun getUsageStats(): Flow<List<UsageEntity>>
    suspend fun saveUsage(usage: UsageEntity)
}
