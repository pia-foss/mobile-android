package com.kape.settings.utils

import kotlinx.serialization.Serializable

@Serializable
data class WireGuardSettings(
    val name: String = "WireGuard",
    var useSmallPackets: Boolean = false,
    val handshake: String = "NOISE_IK",
)