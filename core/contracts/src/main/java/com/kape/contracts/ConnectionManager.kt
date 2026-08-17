package com.kape.contracts

import com.kape.data.vpnserver.VpnServer

interface ConnectionManager {
    suspend fun connect(
        server: VpnServer,
        isManual: Boolean,
        stopCallback: () -> Unit,
        showDialog: () -> Unit,
    )

    /**
     * Connects to the last-selected server, or the optimal one if none was selected yet.
     * Shared by every automatic-reconnect trigger (boot, network change, snooze wake, the
     * system's Always-on VPN start) so they all resolve "what to connect to" the same way.
     */
    suspend fun connectToLastKnownOrOptimalServer()

    suspend fun disconnect()

    suspend fun reconnect(
        server: VpnServer,
        stopCallback: () -> Unit,
    )

    fun isConnectionInProgress(): Boolean
}