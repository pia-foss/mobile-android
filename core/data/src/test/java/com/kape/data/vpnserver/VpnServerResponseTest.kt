package com.kape.data.vpnserver

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class VpnServerResponseTest {
    private val validInfo = VpnServerInfo(autoRegions = listOf("us"), udpPorts = listOf(8080), tcpPorts = listOf(443))
    private val servers = mapOf("us" to vpnServer())

    @Test
    fun `a response with servers and complete info is valid`() {
        assertTrue(VpnServerResponse(servers, validInfo).isValid())
    }

    @Test
    fun `a response without servers is invalid`() {
        assertFalse(VpnServerResponse(null, validInfo).isValid())
        assertFalse(VpnServerResponse(emptyMap(), validInfo).isValid())
    }

    @Test
    fun `a response without info is invalid`() {
        assertFalse(VpnServerResponse(servers, null).isValid())
    }

    @Test
    fun `info missing auto regions or ports is invalid`() {
        assertFalse(validInfo.copy(autoRegions = emptyList()).isValid())
        assertFalse(validInfo.copy(udpPorts = null).isValid())
        assertFalse(validInfo.copy(tcpPorts = emptyList()).isValid())
        assertFalse(VpnServerResponse(servers, validInfo.copy(udpPorts = emptyList())).isValid())
    }
}