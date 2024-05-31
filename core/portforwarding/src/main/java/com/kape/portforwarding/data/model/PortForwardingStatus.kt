package com.kape.portforwarding.data.model

sealed class PortForwardingStatus {
    data object Requesting : PortForwardingStatus()
    data object Error : PortForwardingStatus()
    data object Success : PortForwardingStatus()
    data object NoPortForwarding : PortForwardingStatus()
}