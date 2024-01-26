package com.kape.shareevents.data.models

import com.kape.utils.KoverIgnore

@KoverIgnore("Sealed class, no need for test coverage")
sealed class KpiVpnProtocol(val value: String) {
    data object OpenVpn : KpiVpnProtocol("OpenVPN")
    data object WireGuard : KpiVpnProtocol("WireGuard")
}