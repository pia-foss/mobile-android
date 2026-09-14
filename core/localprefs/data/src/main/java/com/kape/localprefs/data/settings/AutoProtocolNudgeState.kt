package com.kape.vpnconnect.data

import kotlinx.serialization.Serializable

@Serializable
data class AutoProtocolNudgeState(
    val failureTimestamps: List<Long> = emptyList(),
    val promptTimestamps: List<Long> = emptyList(),
    val dismissCount: Int = 0,
    val lastDismissedAt: Long? = null,
    val accepted: Boolean = false,
)