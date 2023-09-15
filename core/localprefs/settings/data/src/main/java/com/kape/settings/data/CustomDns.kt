package com.kape.settings.data

import kotlinx.serialization.Serializable

@Serializable
data class CustomDns(
    val primaryDns: String = "",
    val secondaryDns: String = ""
)
