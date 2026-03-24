package com.kape.connection.model

import android.util.Base64
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Serializable
data class PortBindInformation(
    @SerialName("status")
    val status: String,
    @SerialName("payload")
    val payload: String,
    @SerialName("signature")
    val signature: String,
) {
    @SerialName("decodedPayload")
    val decodedPayload: DecodedPayload =
        Json.decodeFromString(String(Base64.decode(payload, Base64.DEFAULT)))
}