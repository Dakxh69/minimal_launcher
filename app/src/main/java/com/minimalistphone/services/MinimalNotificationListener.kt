package com.minimalistphone.services

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MinimalNotificationListener : NotificationListenerService() {

    @Inject
    lateinit var dataStore: DataStore<Preferences>

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    private val FOCUS_MODE_KEY = booleanPreferencesKey("focus_mode_active")
    private val NOTIF_CONTROL_KEY = stringPreferencesKey("notification_control")

    // Essential apps that should never be blocked (phone, messages, system)
    private val essentialPackages = setOf(
        "com.android.dialer", "com.google.android.dialer",
        "com.android.messaging", "com.google.android.apps.messaging",
        "com.android.systemui", "android"
    )

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        sbn?.let { notification ->
            scope.launch {
                val prefs = dataStore.data.first()
                val notifMode = prefs[NOTIF_CONTROL_KEY] ?: "all"
                val isFocusMode = prefs[FOCUS_MODE_KEY] ?: false
                val pkg = notification.packageName

                val shouldCancel = when {
                    // During focus mode, cancel all non-essential
                    isFocusMode && pkg !in essentialPackages -> true
                    // "none" mode — cancel everything
                    notifMode == "none" -> true
                    // "essential" mode — only allow essential apps
                    notifMode == "essential" && pkg !in essentialPackages -> true
                    // "all" mode — allow everything
                    else -> false
                }

                if (shouldCancel) {
                    cancelNotification(notification.key)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
