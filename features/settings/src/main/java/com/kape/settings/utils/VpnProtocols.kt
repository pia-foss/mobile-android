package com.kape.settings.utils

enum class VpnProtocols(name: String) {
    WireGuard("WireGuard"),
    OpenVPN("OpenVPN"), ;

    companion object {
        fun fromName(name: String) = values().find { it.name == name }
    }
}