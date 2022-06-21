package com.kape.vpn.provider

data class GenericEndpoint(
    val endpoint: String,
    val isProxy: Boolean,
    val usePinnedCertificate: Boolean,
    val certificateCommonName: String?
)