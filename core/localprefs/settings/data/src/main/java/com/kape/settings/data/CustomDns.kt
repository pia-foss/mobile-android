package com.kape.settings.data

import kotlinx.serialization.Serializable

@Serializable
data class CustomDns(
    val primaryDns: String = "",
    val secondaryDns: String = "",
) {
    fun isInUse() = primaryDns.isNotEmpty() || secondaryDns.isNotEmpty()
}
