package com.minimalistphone.domain.usecases

import com.minimalistphone.data.database.dao.BlockedAppDao
import com.minimalistphone.domain.models.LaunchStatus
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey

class CheckAppLaunchStatusUseCase @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val blockedAppDao: BlockedAppDao
) {
    private val FOCUS_MODE_KEY = booleanPreferencesKey("focus_mode_active")

    suspend operator fun invoke(packageName: String): LaunchStatus {
        val prefs = dataStore.data.first()
        val isFocusModeActive = prefs[FOCUS_MODE_KEY] ?: false
        
        val appConfig = blockedAppDao.getAppConfig(packageName)
        
        if (isFocusModeActive && appConfig?.isBlockedDuringFocus == true) {
            return LaunchStatus.BLOCKED
        }
        
        if (appConfig?.requiresDelayToOpen == true) {
            return LaunchStatus.REQUIRES_DELAY
        }
        
        return LaunchStatus.ALLOWED
    }
}
