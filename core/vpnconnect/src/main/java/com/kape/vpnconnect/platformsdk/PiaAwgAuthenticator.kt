package com.kape.vpnconnect.platformsdk

import android.util.Log
import androidx.core.net.toUri
import com.kape.connection.model.AwgObfuscationSettings
import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.platformsdk.vpn.service.models.IpAddress
import com.kape.platformsdk.vpn.wireguard.WireGuardAuthConfiguration
import com.kape.platformsdk.vpn.wireguard.WireGuardAuthenticator
import com.kape.platformsdk.vpn.wireguard.WireGuardEndpointConfiguration
import com.kape.vpnconnect.domain.ConnectionDataSource
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.amnezia.awg.crypto.KeyPair
import java.net.Socket
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.SocketFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

// 138.199.31.184 ("pia-gb-lhr-dp-001") is a standalone AmneziaWG test box outside PIA's normal
// cert-issuance pipeline, so its TLS certificate never passes pinning against configInfo's CA
// (mirrors why the reference curl for this server uses `-sk`). This authenticator skips TLS
// verification entirely via insecureTestClient() below — switch back to CertificatePinningClientImpl
// (see PiaWgAuthenticator) once add-awg-key is served from a properly PIA-signed host.
class PiaAwgAuthenticator(
    private val connectionSource: ConnectionDataSource,
    private val connectionPrefs: ConnectionPrefs,
    // The addKey request must reach the server before that server's own tunnel exists, so its
    // socket needs protect()-ing to bypass the kill-switch's network-lock tunnel — otherwise it's
    // captured and silently dropped, and every WireGuard connection attempt times out.
    protect: (Socket) -> Boolean,
) : WireGuardAuthenticator {
    private val client = insecureTestClient(protect)

    override suspend fun authenticate(endpointConfiguration: WireGuardEndpointConfiguration): WireGuardAuthConfiguration {
        val authIp = endpointConfiguration.authIp.asString()

        val keyPair = KeyPair()
        val url =
            "https://$authIp:${endpointConfiguration.authPort}/$ADD_KEY_PATH"
                .toUri()
                .buildUpon()
                .appendQueryParameter(TOKEN_PARAM, connectionSource.getVpnToken())
                .appendQueryParameter(PUBLIC_KEY_PARAM, keyPair.publicKey.toBase64())
                .build()
                .toString()

        val response =
            try {
                client.get(url)
            } catch (e: Exception) {
                Log.e(TAG, "add-awg-key request to $authIp failed", e)
                throw e
            }
        val responseBody = response.bodyAsText()
        Log.d(TAG, "add-awg-key response: status=${response.status} body=$responseBody")
        check(response.status == HttpStatusCode.OK) {
            "add-awg-key request to $authIp failed with status ${response.status}"
        }
        val addKeyResponse = json.decodeFromString<AwgAddKeyResponse>(responseBody)
        connectionPrefs.setGateway(addKeyResponse.serverVip)
        connectionPrefs.setAwgObfuscation(addKeyResponse.obfuscation?.toSettings())
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
        private const val TAG = "PiaAwgAuthenticator"
        private const val ADD_KEY_PATH = "add-awg-key"
        private const val TOKEN_PARAM = "pt"
        private const val PUBLIC_KEY_PARAM = "pubkey"

        // wireguard-go's own default for an unset preshared key is 32 zero bytes — PIA's addKey
        // endpoint never issues one, and WireGuardAuthConfiguration.psk isn't nullable like the
        // SDK's presharedKeyBase64 field, so this reproduces "no PSK" without SDK support for
        // actually omitting it.
        private const val NO_PRESHARED_KEY_BASE64 = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA="
    }
}

// Deliberately trusts every certificate and hostname — see the class-level comment on
// PiaAwgAuthenticator for why pinning can't be used against this test server.
private fun insecureTestClient(protect: (Socket) -> Boolean): HttpClient {
    val trustAllCertificates =
        object : X509TrustManager {
            override fun checkClientTrusted(
                chain: Array<X509Certificate>,
                authType: String,
            ) = Unit

            override fun checkServerTrusted(
                chain: Array<X509Certificate>,
                authType: String,
            ) = Unit

            override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
        }
    val sslContext =
        SSLContext.getInstance("TLS").apply {
            init(null, arrayOf(trustAllCertificates), SecureRandom())
        }

    return HttpClient(OkHttp) {
        engine {
            config {
                sslSocketFactory(sslContext.socketFactory, trustAllCertificates)
                hostnameVerifier { _, _ -> true }
                socketFactory(protectingSocketFactory(protect))
            }
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 8000
        }
    }
}

// A SocketFactory that hands out sockets pre-protected via `protect`, so the resulting connection
// routes over the underlying network instead of being captured by the kill switch's network-lock
// tunnel. OkHttp's connection setup calls the no-arg createSocket() and connects it itself; the
// host/port overloads are implemented for interface completeness and aren't exercised in practice.
private fun protectingSocketFactory(protect: (Socket) -> Boolean): SocketFactory =
    object : SocketFactory() {
        override fun createSocket(): Socket =
            Socket().apply {
                bind(java.net.InetSocketAddress(0))
                protect(this)
            }

        override fun createSocket(
            host: String,
            port: Int,
        ): Socket = createSocket().apply { connect(java.net.InetSocketAddress(host, port)) }

        override fun createSocket(
            host: String,
            port: Int,
            localHost: java.net.InetAddress,
            localPort: Int,
        ): Socket =
            createSocket().apply {
                bind(java.net.InetSocketAddress(localHost, localPort))
                connect(java.net.InetSocketAddress(host, port))
            }

        override fun createSocket(
            host: java.net.InetAddress,
            port: Int,
        ): Socket = createSocket().apply { connect(java.net.InetSocketAddress(host, port)) }

        override fun createSocket(
            address: java.net.InetAddress,
            port: Int,
            localAddress: java.net.InetAddress,
            localPort: Int,
        ): Socket =
            createSocket().apply {
                bind(java.net.InetSocketAddress(localAddress, localPort))
                connect(java.net.InetSocketAddress(address, port))
            }
    }

@Serializable
internal data class AwgAddKeyResponse(
    @SerialName("status")
    val status: String,
    @SerialName("server_key")
    val serverKey: String,
    @SerialName("server_ip")
    val serverIp: String,
    @SerialName("server_port")
    val serverPort: Int,
    @SerialName("server_vip")
    val serverVip: String,
    @SerialName("peer_ip")
    val peerIp: String,
    @SerialName("peer_pubkey")
    val peerPubKey: String,
    @SerialName("dns_servers")
    val dnsServers: List<String> = emptyList(),
    @SerialName("obfuscation")
    val obfuscation: AwgObfuscationResponse? = null,
)

@Serializable
internal data class AwgObfuscationResponse(
    @SerialName("jc")
    val jc: Long,
    @SerialName("jmin")
    val jmin: Long,
    @SerialName("jmax")
    val jmax: Long,
    @SerialName("s1")
    val s1: Long,
    @SerialName("s2")
    val s2: Long,
    @SerialName("h1")
    val h1: Long,
    @SerialName("h2")
    val h2: Long,
    @SerialName("h3")
    val h3: Long,
    @SerialName("h4")
    val h4: Long,
) {
    fun toSettings() =
        AwgObfuscationSettings(
            junkPacketCount = jc,
            junkPacketMinSize = jmin,
            junkPacketMaxSize = jmax,
            initPacketJunkSize = s1,
            responsePacketJunkSize = s2,
            initPacketMagicHeader = h1,
            responsePacketMagicHeader = h2,
            underloadPacketMagicHeader = h3,
            transportPacketMagicHeader = h4,
        )
}