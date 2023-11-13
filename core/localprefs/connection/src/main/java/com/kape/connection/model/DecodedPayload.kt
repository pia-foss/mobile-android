package com.kape.connection.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DecodedPayload(
    @SerialName("port")
    val port: Int,
    @SerialName("token")
    val token: String,
    @SerialName("expirationDate")
    val expirationDate: String,
)