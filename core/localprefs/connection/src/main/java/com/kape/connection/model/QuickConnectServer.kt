package com.kape.connection.model

import kotlinx.serialization.Serializable

@Serializable
data class QuickConnectServer(val serverKey: String, val isDip: Boolean = false)