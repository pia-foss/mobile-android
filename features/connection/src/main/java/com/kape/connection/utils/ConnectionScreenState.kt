package com.kape.connection.utils

import com.kape.rating.data.RatingDialogType
import com.kape.utils.vpnserver.VpnServer

data class ConnectionScreenState(
    val server: VpnServer,
    val quickConnectServers: List<VpnServer>,
    val isCurrentServerOptimal: Boolean,
    val showOptimalLocationInfo: Boolean,
    val ratingDialogType: RatingDialogType?,
)