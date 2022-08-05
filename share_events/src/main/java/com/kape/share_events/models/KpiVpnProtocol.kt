package com.kape.share_events.models

sealed class KpiVpnProtocol(val value: String) {
    object OpenVpn: KpiVpnProtocol("OpenVPN")
    object WireGuard: KpiVpnProtocol("WireGuard")
}