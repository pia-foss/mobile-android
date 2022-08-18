package com.kape.connection.utils

import com.kape.region_selection.server.Server

// TODO: temporary states, will evolve
data class SnoozeState(val active: Boolean, val activeUntil: String? = null)

val SNOOZE_STATE_DEFAULT = SnoozeState(active = false)

data class ConnectionScreenState(val selectedServer: Server? = null, val snoozeState: SnoozeState)

val IDLE = ConnectionScreenState(snoozeState = SNOOZE_STATE_DEFAULT)