package com.kape.portforwarding.data.model

import com.kape.connection.model.DecodedPayload
import kotlinx.serialization.Serializable

@Serializable
data class PayloadAndSignature(
    val payload: DecodedPayload,
    val signature: String,
)