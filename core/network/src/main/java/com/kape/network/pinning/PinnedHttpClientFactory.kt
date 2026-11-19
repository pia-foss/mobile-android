package com.kape.network.pinning

import io.ktor.client.HttpClient
import java.net.Socket

interface PinnedHttpClientFactory {
    fun create(
        knownEndpoints: List<EndpointIdentity>,
        socketProtector: SocketProtector? = null,
    ): HttpClient
}

data class EndpointIdentity(
    val endpoint: String,
    val commonName: String,
)

fun interface SocketProtector {
    fun protect(socket: Socket): Boolean
}