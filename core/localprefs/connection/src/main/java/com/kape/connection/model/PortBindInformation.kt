package com.kape.connection.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PortBindInformation(
    @SerialName("payload")
    val payload: String,
    @SerialName("signature")
    val signature: String,
    @SerialName("decodedPayload")
    val decodedPayload: DecodedPayload,
)