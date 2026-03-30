package com.kape.contracts.data.kpi

sealed class KpiConnectionEvent(val value: String) {
    data object ConnectionAttempt : KpiConnectionEvent("VPN_CONNECTION_ATTEMPT")
    data object ConnectionCancelled : KpiConnectionEvent("VPN_CONNECTION_CANCELLED")
    data object ConnectionEstablished : KpiConnectionEvent("VPN_CONNECTION_ESTABLISHED")
}