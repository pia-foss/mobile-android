package com.kape.data.kpi

sealed class KpiVpnProtocol(val value: String) {
    data object OpenVpn : KpiVpnProtocol("OpenVPN")
    data object WireGuard : KpiVpnProtocol("WireGuard")
}