package com.kape.portforwarding.data.model

import kotlinx.serialization.Serializable

@Serializable
data class BindPortResponse(val protocol: String, val code: Int, val message: String, val url: String)