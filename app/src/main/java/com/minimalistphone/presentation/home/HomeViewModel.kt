package com.minimalistphone.presentation.home

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minimalistphone.domain.models.AppInfo
import com.minimalistphone.domain.models.LaunchStatus
import com.minimalistphone.domain.repository.AppRepository
import com.minimalistphone.domain.usecases.CheckAppLaunchStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AppLaunchEvent {
    data class RequiresDelay(val packageName: String) : AppLaunchEvent()
    data class Blocked(val packageName: String) : AppLaunchEvent()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val appRepository: AppRepository,
    private val checkAppLaunchStatusUseCase: CheckAppLaunchStatusUseCase,
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    companion object {
        val PINNED_APPS_KEY = stringSetPreferencesKey("pinned_apps")
        val HIDDEN_APPS_KEY = stringSetPreferencesKey("hidden_apps")
        val BLOCKED_APPS_KEY = stringSetPreferencesKey("blocked_apps_set")
        val FOCUS_MODE_KEY = booleanPreferencesKey("focus_mode_active")
        val NOTIF_CONTROL_KEY = stringPreferencesKey("notification_control")
        val GRAYSCALE_KEY = booleanPreferencesKey("grayscale_mode")
        val MINDFULNESS_KEY = booleanPreferencesKey("mindfulness_prompts")
    }

    private val _allApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val allApps: StateFlow<List<AppInfo>> = _allApps.asStateFlow()

    private val _launchEvent = MutableSharedFlow<AppLaunchEvent>()
    val launchEvent: SharedFlow<AppLaunchEvent> = _launchEvent.asSharedFlow()

    val pinnedPackages: StateFlow<Set<String>> = dataStore.data
        .map { it[PINNED_APPS_KEY] ?: emptySet() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    val hiddenPackages: StateFlow<Set<String>> = dataStore.data
        .map { it[HIDDEN_APPS_KEY] ?: emptySet() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    val blockedPackages: StateFlow<Set<String>> = dataStore.data
        .map { it[BLOCKED_APPS_KEY] ?: emptySet() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    val focusModeActive: StateFlow<Boolean> = dataStore.data
        .map { it[FOCUS_MODE_KEY] ?: false }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    // "all", "essential", "none"
    val notifControl: StateFlow<String> = dataStore.data
        .map { it[NOTIF_CONTROL_KEY] ?: "all" }
        .stateIn(viewModelScope, SharingStarted.Eagerly, "all")

    val grayscaleMode: StateFlow<Boolean> = dataStore.data
        .map { it[GRAYSCALE_KEY] ?: false }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val mindfulnessPrompts: StateFlow<Boolean> = dataStore.data
        .map { it[MINDFULNESS_KEY] ?: false }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val pinnedApps: StateFlow<List<AppInfo>> = combine(_allApps, pinnedPackages) { apps, pinned ->
        if (pinned.isEmpty()) return@combine emptyList()
        val pinnedSet = pinned.toHashSet()
        apps.filter { it.packageName in pinnedSet }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Apps visible in search = all - hidden
    val searchableApps: StateFlow<List<AppInfo>> = combine(_allApps, hiddenPackages) { apps, hidden ->
        if (hidden.isEmpty()) return@combine apps
        val hiddenSet = hidden.toHashSet()
        apps.filter { it.packageName !in hiddenSet }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        loadApps()
    }

    private fun loadApps() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            // Load pinned app names from DataStore first for instant display
            val prefs = dataStore.data.first()
            val pinnedSet = prefs[PINNED_APPS_KEY]

            val apps = appRepository.getInstalledApps()

            if (pinnedSet == null) {
                val defaultPinned = apps.take(6).map { it.packageName }.toSet()
                dataStore.edit { it[PINNED_APPS_KEY] = defaultPinned }
                // Show pinned immediately
                _allApps.value = apps
            } else {
                // Show pinned apps first, then full list
                val pinnedAppsOnly = apps.filter { it.packageName in pinnedSet }
                _allApps.value = pinnedAppsOnly
                // Then load full list
                _allApps.value = apps
            }
        }
    }

    fun onAppClicked(packageName: String) {
        viewModelScope.launch {
            val isBlocked = packageName in blockedPackages.value
            val isFocusActive = focusModeActive.value

            if (isFocusActive && isBlocked) {
                // Fully blocked during focus — show blocked message
                _launchEvent.emit(AppLaunchEvent.Blocked(packageName))
                return@launch
            }

            if (isBlocked) {
                // Not in focus mode but app is in blocked list — show delay
                _launchEvent.emit(AppLaunchEvent.RequiresDelay(packageName))
                return@launch
            }

            // Normal launch
            appRepository.launchApp(packageName)
        }
    }

    fun launchAppDirectly(packageName: String) {
        appRepository.launchApp(packageName)
    }

    fun togglePinned(packageName: String) {
        viewModelScope.launch {
            dataStore.edit { prefs ->
                val current = prefs[PINNED_APPS_KEY] ?: emptySet()
                prefs[PINNED_APPS_KEY] = if (packageName in current) current - packageName else current + packageName
            }
        }
    }

    fun toggleHidden(packageName: String) {
        viewModelScope.launch {
            dataStore.edit { prefs ->
                val current = prefs[HIDDEN_APPS_KEY] ?: emptySet()
                prefs[HIDDEN_APPS_KEY] = if (packageName in current) current - packageName else current + packageName
            }
        }
    }

    fun toggleBlocked(packageName: String) {
        viewModelScope.launch {
            dataStore.edit { prefs ->
                val current = prefs[BLOCKED_APPS_KEY] ?: emptySet()
                prefs[BLOCKED_APPS_KEY] = if (packageName in current) current - packageName else current + packageName
            }
        }
    }

    fun setFocusMode(active: Boolean) {
        viewModelScope.launch {
            dataStore.edit { it[FOCUS_MODE_KEY] = active }
        }
    }

    fun cycleNotifControl() {
        viewModelScope.launch {
            dataStore.edit { prefs ->
                val current = prefs[NOTIF_CONTROL_KEY] ?: "all"
                prefs[NOTIF_CONTROL_KEY] = when (current) {
                    "all" -> "essential"
                    "essential" -> "none"
                    else -> "all"
                }
            }
        }
    }

    fun toggleGrayscale() {
        viewModelScope.launch {
            dataStore.edit { it[GRAYSCALE_KEY] = !(it[GRAYSCALE_KEY] ?: false) }
        }
    }

    fun toggleMindfulness() {
        viewModelScope.launch {
            dataStore.edit { it[MINDFULNESS_KEY] = !(it[MINDFULNESS_KEY] ?: false) }
        }
    }
}
