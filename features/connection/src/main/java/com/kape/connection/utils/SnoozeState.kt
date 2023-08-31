package com.kape.connection.utils

data class SnoozeState(val active: Boolean, val activeUntil: String? = null)

val SNOOZE_STATE_DEFAULT = SnoozeState(active = false)

enum class SnoozeInterval(val value: Int) {
    SNOOZE_DEFAULT_MS(0),
    SNOOZE_SHORT_MS(5 * 60 * 1000),
    SNOOZE_MEDIUM_MS(15 * 60 * 1000),
    SNOOZE_LONG_MS(60 * 60 * 1000), ;
}