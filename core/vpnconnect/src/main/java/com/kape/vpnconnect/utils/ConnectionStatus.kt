package com.kape.vpnconnect.utils

sealed class ConnectionStatus {
    data object DISCONNECTED : ConnectionStatus()
    data object CONNECTED : ConnectionStatus()
    data object CONNECTING : ConnectionStatus()
    data object RECONNECTING : ConnectionStatus()
    data object ERROR : ConnectionStatus()
}