package com.kape.shareevents.data.models

sealed class KpiEventPropertyKey(val value: String) {
    data object ConnectionSource : KpiEventPropertyKey("connection_source")
    data object UserAgent : KpiEventPropertyKey("user_agent")
    data object VpnProtocol : KpiEventPropertyKey("vpn_protocol")
    data object TimeToConnect : KpiEventPropertyKey("time_to_connect")
}