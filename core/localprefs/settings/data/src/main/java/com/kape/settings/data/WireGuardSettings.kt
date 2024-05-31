package com.kape.settings.data

import kotlinx.serialization.Serializable

@Serializable
data class WireGuardSettings(
    override val name: String = "WireGuard",
    override val auth: String = "Poly1305",
    override var transport: Transport = Transport.UDP,
    override var dataEncryption: DataEncryption = DataEncryption.CHA_CHA_20,
    override var port: String = "1337",
    override var useSmallPackets: Boolean = false,
    override var handshake: String = "NOISE_IK",
    override var mtu: Int = if (useSmallPackets) 1280 else 1420,
) : ProtocolSettings