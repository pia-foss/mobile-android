package com.kape.shareevents.data.models

sealed class KpiVpnProtocol(val value: String) {
    data object OpenVpn : KpiVpnProtocol("OpenVPN")
    data object WireGuard : KpiVpnProtocol("WireGuard")
}