package com.kape.shareevents.data.models

sealed class KpiConnectionStatus {
    object NotConnected : KpiConnectionStatus()
    object Started : KpiConnectionStatus()
    object Connecting : KpiConnectionStatus()
    object Reconnecting : KpiConnectionStatus()
    object Failed : KpiConnectionStatus()
    object Connected : KpiConnectionStatus()
}