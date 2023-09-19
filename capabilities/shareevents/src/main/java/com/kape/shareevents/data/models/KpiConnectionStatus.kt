package com.kape.shareevents.data.models

sealed class KpiConnectionStatus {
    data object NotConnected : KpiConnectionStatus()
    data object Connecting : KpiConnectionStatus()
    data object Reconnecting : KpiConnectionStatus()
    data object Connected : KpiConnectionStatus()
}