package com.kape.portforwarding.data

import androidx.core.net.toUri
import com.kape.connection.model.PortBindInformation
import com.kape.contracts.di.Qualifiers
import com.kape.network.pinning.EndpointIdentity
import com.kape.network.pinning.PinnedHttpClientFactory
import com.kape.portforwarding.domain.PortForwardingApi
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Named
import org.koin.core.annotation.Singleton

private const val PORT_FORWARDING_PORT = 19999

@Singleton(binds = [PortForwardingApi::class])
internal class GatewayPortForwardingApi(
    private val clients: PinnedHttpClientFactory,
    @Named(Qualifiers.USER_AGENT) private val userAgent: String,
) : PortForwardingApi {
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun getPayloadAndSignature(
        vpnToken: String,
        gatewayIdentities: List<EndpointIdentity>,
    ): PortBindInformation? {
        val gateway = gatewayIdentities.firstOrNull()?.endpoint ?: return null
        val url =
            gatewayUrl(gateway, "getSignature")
                .buildUpon()
                .appendQueryParameter("token", vpnToken)
                .build()
                .toString()
        return runCatching {
            // No socket protector: these requests must go through the tunnel to reach the gateway.
            clients.create(gatewayIdentities).use { client ->
                val response = client.get(url) { header(HttpHeaders.UserAgent, userAgent) }
                if (response.status != HttpStatusCode.OK) return@use null
                json.decodeFromString<PortBindInformation>(response.bodyAsText()).takeIf { it.status == "OK" }
            }
        }.getOrNull()
    }

    override suspend fun bindPort(
        token: String,
        payload: String,
        signature: String,
        gatewayIdentities: List<EndpointIdentity>,
    ): Boolean {
        val gateway = gatewayIdentities.firstOrNull()?.endpoint ?: return false
        val url =
            gatewayUrl(gateway, "bindPort")
                .buildUpon()
                .appendQueryParameter("payload", payload)
                .appendQueryParameter("signature", signature)
                .build()
                .toString()
        return runCatching {
            clients.create(gatewayIdentities).use { client ->
                client
                    .get(url) {
                        header(HttpHeaders.UserAgent, userAgent)
                        header(HttpHeaders.Authorization, token)
                    }.status == HttpStatusCode.OK
            }
        }.getOrDefault(false)
    }

    private fun gatewayUrl(
        gateway: String,
        path: String,
    ) = "https://$gateway:$PORT_FORWARDING_PORT/$path".toUri()
}