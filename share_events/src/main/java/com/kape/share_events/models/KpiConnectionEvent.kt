package com.kape.share_events.models

sealed class KpiConnectionEvent(val value: String) {
    object ConnectionAttempt : KpiConnectionEvent("VPN_CONNECTION_ATTEMPT")
    object ConnectionCancelled : KpiConnectionEvent("VPN_CONNECTION_CANCELLED")
    object ConnectionEstablished : KpiConnectionEvent("VPN_CONNECTION_ESTABLISHED")
}