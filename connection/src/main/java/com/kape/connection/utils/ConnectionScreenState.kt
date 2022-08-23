package com.kape.connection.utils

import com.kape.region_selection.server.Server

// TODO: temporary states, will evolve
data class SnoozeState(val active: Boolean, val activeUntil: String? = null)

val SNOOZE_STATE_DEFAULT = SnoozeState(active = false)

data class UsageState(val download: Int, val upload: Int)

val USAGE_STATE_DEFAULT = UsageState(0, 0)

data class ConnectionScreenState(
    val selectedServer: Server? = null,
    val snoozeState: SnoozeState,
    val usageState: UsageState,
    val favoriteServers: List<Server> = emptyList()
)

val IDLE =
    ConnectionScreenState(snoozeState = SNOOZE_STATE_DEFAULT, usageState = USAGE_STATE_DEFAULT)

