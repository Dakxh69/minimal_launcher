package com.minimalistphone.domain.models

data class AppInfo(
    val packageName: String,
    val label: String,
    val isPinned: Boolean = false
)

enum class LaunchStatus {
    ALLOWED,
    BLOCKED,
    REQUIRES_DELAY
}
