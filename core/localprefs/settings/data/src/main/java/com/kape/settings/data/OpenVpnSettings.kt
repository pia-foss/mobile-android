package com.kape.settings.data

import kotlinx.serialization.Serializable

@Serializable
data class OpenVpnSettings(
    val name: String = "OpenVPN",
    var transport: Transport = Transport.UDP,
    var dataEncryption: DataEncryption = DataEncryption.AES_128_GCM,
    var port: String = "8080",
    var useSmallPackets: Boolean = false,
    var handshake: String = "RSA4096",
)

enum class Transport(val value: String) {
    UDP("UDP"),
    TCP("TCP"), ;

    companion object {
        fun fromName(name: String) = Transport.values().find { it.value == name }
    }
}

enum class DataEncryption(val value: String) {
    AES_128_GCM("AES-128-GCM"),
    AES_256_GCM("AES-256-GCM"), ;

    companion object {
        fun fromName(name: String) = DataEncryption.values().find { it.name == name }
    }
}