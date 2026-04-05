package com.kape.connection.utils

import com.kape.data.vpnserver.VpnServer
import com.kape.rating.data.RatingDialogType

data class ConnectionScreenState(
    val server: VpnServer,
    val quickConnectServers: List<VpnServer>,
    val isCurrentServerOptimal: Boolean,
    val showOptimalLocationInfo: Boolean,
    val ratingDialogType: RatingDialogType?,
)