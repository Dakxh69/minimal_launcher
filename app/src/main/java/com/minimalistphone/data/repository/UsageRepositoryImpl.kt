package com.minimalistphone.data.repository

import com.minimalistphone.data.database.dao.UsageDao
import com.minimalistphone.data.models.UsageEntity
import com.minimalistphone.domain.repository.UsageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UsageRepositoryImpl @Inject constructor(
    private val usageDao: UsageDao
) : UsageRepository {
    
    override fun getUsageStats(): Flow<List<UsageEntity>> {
        return usageDao.getAllUsageStats()
    }

    override suspend fun saveUsage(usage: UsageEntity) {
        usageDao.insertUsage(usage)
    }
}
