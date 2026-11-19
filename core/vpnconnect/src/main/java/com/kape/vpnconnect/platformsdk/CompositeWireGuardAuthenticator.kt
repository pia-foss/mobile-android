package com.kape.vpnconnect.platformsdk

import com.kape.platformsdk.vpn.wireguard.WireGuardAuthConfiguration
import com.kape.platformsdk.vpn.wireguard.WireGuardAuthenticator
import com.kape.platformsdk.vpn.wireguard.WireGuardEndpointConfiguration
import com.kape.platformsdk.vpn.wireguard.WireGuardObfuscation

// Automatic mode can mix AMNEZIA and WIREGUARD endpoints in one attempt list, so the authenticator
// has to be picked per attempt from the endpoint, not once from the selected protocol.
class CompositeWireGuardAuthenticator(
    private val wgAuthenticator: WireGuardAuthenticator,
    private val awgAuthenticator: WireGuardAuthenticator,
) : WireGuardAuthenticator {
    override suspend fun authenticate(endpointConfiguration: WireGuardEndpointConfiguration): WireGuardAuthConfiguration =
        when (endpointConfiguration.obfuscation) {
            is WireGuardObfuscation.Amnezia -> awgAuthenticator.authenticate(endpointConfiguration)
            is WireGuardObfuscation.None, is WireGuardObfuscation.Unknown -> wgAuthenticator.authenticate(endpointConfiguration)
        }
}