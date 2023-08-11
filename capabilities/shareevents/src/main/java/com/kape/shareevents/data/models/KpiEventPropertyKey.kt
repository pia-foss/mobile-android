package com.kape.shareevents.data.models

sealed class KpiEventPropertyKey(val value: String) {
    object ConnectionSource : KpiEventPropertyKey("connection_source")
    object UserAgent : KpiEventPropertyKey("user_agent")
    object VpnProtocol : KpiEventPropertyKey("vpn_protocol")
    object TimeToConnect : KpiEventPropertyKey("time_to_connect")
}