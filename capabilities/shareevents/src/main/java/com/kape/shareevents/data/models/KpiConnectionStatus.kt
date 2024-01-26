package com.kape.shareevents.data.models

import com.kape.utils.KoverIgnore

@KoverIgnore("Sealed class, no need for test coverage")
sealed class KpiConnectionStatus {
    data object NotConnected : KpiConnectionStatus()
    data object Connecting : KpiConnectionStatus()
    data object Reconnecting : KpiConnectionStatus()
    data object Connected : KpiConnectionStatus()
}