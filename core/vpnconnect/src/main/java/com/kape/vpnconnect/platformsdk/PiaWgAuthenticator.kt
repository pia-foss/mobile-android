package com.kape.vpnconnect.platformsdk

import android.util.Base64
import androidx.core.net.toUri
import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.network.pinning.EndpointIdentity
import com.kape.network.pinning.PinnedHttpClientFactory
import com.kape.network.pinning.SocketProtector
import com.kape.platformsdk.vpn.service.models.IpAddress
import com.kape.platformsdk.vpn.wireguard.WireGuardAuthConfiguration
import com.kape.platformsdk.vpn.wireguard.WireGuardAuthenticator
import com.kape.platformsdk.vpn.wireguard.WireGuardEndpointConfiguration
import com.kape.vpnconnect.domain.ConnectionDataSource
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.amnezia.awg.crypto.KeyPair
import java.net.Socket

class PiaWgAuthenticator(
    private val clients: PinnedHttpClientFactory,
    private val connectionSource: ConnectionDataSource,
    private val connectionPrefs: ConnectionPrefs,
    private val protect: (Socket) -> Boolean,
) : WireGuardAuthenticator {
    private val pinnedClients = mutableMapOf<EndpointIdentity, HttpClient>()

    private fun clientFor(identity: EndpointIdentity): HttpClient =
        synchronized(pinnedClients) {
            pinnedClients.getOrPut(identity) { clients.create(listOf(identity), SocketProtector { protect(it) }) }
        }

    override suspend fun authenticate(endpointConfiguration: WireGuardEndpointConfiguration): WireGuardAuthConfiguration {
        val authIp = endpointConfiguration.authIp.asString()

        val client = clientFor(EndpointIdentity(endpoint = authIp, commonName = endpointConfiguration.certDn))

        val keyPair = KeyPair()
        val vpnTokenBase64 = Base64.encodeToString(connectionSource.getVpnToken().toByteArray(), Base64.NO_WRAP)
        val url =
            "https://$authIp:${endpointConfiguration.authPort}/$ADD_KEY_PATH"
                .toUri()
                .buildUpon()
                .appendQueryParameter(PUBLIC_KEY_PARAM, keyPair.publicKey.toBase64())
                .build()
                .toString()

        val response =
            client.get(url) {
                header("Authorization", "Basic $vpnTokenBase64")
            }
        check(response.status == HttpStatusCode.OK) {
            "addKey request to $authIp failed with status ${response.status}"
        }
        val addKeyResponse = json.decodeFromString<WireguardAddKeyResponse>(response.body())
        connectionPrefs.setGateway(addKeyResponse.serverVip)
        return WireGuardAuthConfiguration(
            psk = NO_PRESHARED_KEY_BASE64,
            serverPublicKey = addKeyResponse.serverKey,
            clientPrivateKey = keyPair.privateKey.toBase64(),
            internalIp = addKeyResponse.peerIp,
            dnsServers = addKeyResponse.dnsServers,
            gatewayIp = IpAddress.V4(addKeyResponse.serverVip),
        )
    }

    private fun IpAddress.asString(): String =
        when (this) {
            is IpAddress.V4 -> value
            is IpAddress.V6 -> value
        }

    companion object {
        private val json = Json { ignoreUnknownKeys = true }
        private const val ADD_KEY_PATH = "addKey"
        private const val PUBLIC_KEY_PARAM = "pubkey"

        private const val NO_PRESHARED_KEY_BASE64 = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA="
    }
}

@Serializable
internal data class WireguardAddKeyResponse(
    @SerialName("peer_ip")
    val peerIp: String,
    @SerialName("peer_pubkey")
    val peerPubKey: String,
    @SerialName("server_ip")
    val serverIp: String,
    @SerialName("server_key")
    val serverKey: String,
    @SerialName("server_port")
    val serverPort: Int,
    @SerialName("server_vip")
    val serverVip: String,
    @SerialName("status")
    val status: String,
    @SerialName("dns_servers")
    val dnsServers: List<String> = emptyList(),
)