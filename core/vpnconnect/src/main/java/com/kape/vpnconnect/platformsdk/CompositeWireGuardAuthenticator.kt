package com.kape.vpnconnect.platformsdk

import com.kape.platformsdk.vpn.wireguard.WireGuardAuthConfiguration
import com.kape.platformsdk.vpn.wireguard.WireGuardAuthenticator
import com.kape.platformsdk.vpn.wireguard.WireGuardEndpointConfiguration
import com.kape.platformsdk.vpn.wireguard.WireGuardObfuscation

// Automatic mode can hand the session's single WireGuard controller a mix of AMNEZIA-endpoint and
// WIREGUARD-endpoint configurations in the same attempt list (or fall back from one to the other),
// so the authenticator can't be chosen once up front from the selected protocol/country — it has
// to be picked per attempt from the endpoint it's actually being asked to authenticate against.
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