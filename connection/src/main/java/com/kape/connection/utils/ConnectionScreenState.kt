package com.kape.connection.utils

import com.kape.region_selection.server.Server

// TODO: temporary state, will update when more functionality is available
data class ConnectionScreenState(val selectedServer: Server? = null)

val IDLE = ConnectionScreenState()