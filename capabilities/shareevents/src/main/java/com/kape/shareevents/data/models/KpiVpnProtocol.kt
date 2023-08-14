package com.kape.shareevents.data.models

sealed class KpiVpnProtocol(val value: String) {
    object OpenVpn : KpiVpnProtocol("OpenVPN")
    object WireGuard : KpiVpnProtocol("WireGuard")
}