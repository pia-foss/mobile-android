package com.kape.shareevents.data.models

import com.kape.utils.KoverIgnore

@KoverIgnore("Sealed class, no need for test coverage")
sealed class KpiConnectionEvent(val value: String) {
    data object ConnectionAttempt : KpiConnectionEvent("VPN_CONNECTION_ATTEMPT")
    data object ConnectionCancelled : KpiConnectionEvent("VPN_CONNECTION_CANCELLED")
    data object ConnectionEstablished : KpiConnectionEvent("VPN_CONNECTION_ESTABLISHED")
}