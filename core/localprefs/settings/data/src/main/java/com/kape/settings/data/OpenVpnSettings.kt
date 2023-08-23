package com.kape.settings.data

import kotlinx.serialization.Serializable

@Serializable
data class OpenVpnSettings(
    val name: String = "OpenVPN",
    var transport: Transport = Transport.UDP,
    var dataEncryption: DataEncryption = DataEncryption.AES_128_GCM,
    var port: String = "",
    var useSmallPackets: Boolean = false,
    var handshake: String = "RSA4096",
)

enum class Transport(name: String) {
    UDP("UDP"),
    TCP("TCP"), ;

    companion object {
        fun fromName(name: String) = Transport.values().find { it.name == name }
    }
}

enum class DataEncryption(name: String) {
    AES_128_GCM("AES_128_GCM"),
    AES_256_GCM("AES_256_GCM"), ;

    companion object {
        fun fromName(name: String) = DataEncryption.values().find { it.name == name }
    }
}