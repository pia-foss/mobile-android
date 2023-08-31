package com.kape.settings.data

import kotlinx.serialization.Serializable

@Serializable
data class OpenVpnSettings(
    override val name: String = "OpenVPN",
    override val auth: String = "SHA256",
    override var transport: Transport = Transport.UDP,
    override var dataEncryption: DataEncryption = DataEncryption.AES_128_GCM,
    override var port: String = "8080",
    override var useSmallPackets: Boolean = false,
    override var handshake: String = "RSA4096",
): ProtocolSettings