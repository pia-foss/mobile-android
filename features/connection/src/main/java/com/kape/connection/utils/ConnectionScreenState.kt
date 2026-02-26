package com.kape.connection.utils

import com.kape.rating.data.RatingDialogType
import com.kape.utils.vpnserver.VpnServer
import com.kape.vpnconnect.utils.ConnectionStatus

data class ConnectionScreenState(
    val server: VpnServer,
    val quickConnectServers: List<VpnServer>,
    val isCurrentServerOptimal: Boolean,
    val showOptimalLocationInfo: Boolean,
    val ratingDialogType: RatingDialogType?,
    val connectionData: ConnectionData,
)

data class ConnectionData(
    val clientIp: String,
    val vpnIp: String,
    val connectionStatus: ConnectionStatus,
)