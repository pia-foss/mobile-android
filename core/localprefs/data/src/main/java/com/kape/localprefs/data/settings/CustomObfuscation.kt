package com.kape.settings.data

import kotlinx.serialization.Serializable

@Serializable
data class CustomObfuscation(
    val host: String,
    val port: String,
    val key: String,
    val cipher: String
) {
    fun isValid() = host.isNotEmpty() && port.isNotEmpty() && key.isNotEmpty() && cipher.isNotEmpty()
}