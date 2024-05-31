package com.kape.utils.shadowsocksserver

import kotlinx.serialization.Serializable

@Serializable
public data class ShadowsocksServer(
    val iso: String = "world",
    val region: String,
    val host: String,
    val port: Int,
    val key: String,
    val cipher: String,
)