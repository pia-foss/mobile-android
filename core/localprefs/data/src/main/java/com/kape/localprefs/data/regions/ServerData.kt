package com.kape.regions.data

import kotlinx.serialization.Serializable

@Serializable
data class ServerData(val name: String, val isDip: Boolean)