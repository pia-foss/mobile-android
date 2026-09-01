package com.kape.vpnconnect.platformsdk

import android.util.Log
import androidx.core.net.toUri
import com.kape.connection.model.AwgObfuscationSettings
import com.kape.httpclient.data.CertificatePinningClientImpl
import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.platformsdk.vpn.service.models.IpAddress
import com.kape.platformsdk.vpn.wireguard.WireGuardAuthConfiguration
import com.kape.platformsdk.vpn.wireguard.WireGuardAuthenticator
import com.kape.platformsdk.vpn.wireguard.WireGuardEndpointConfiguration
import com.kape.vpnconnect.domain.ConnectionDataSource
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.amnezia.awg.crypto.KeyPair
import java.net.Socket

class PiaAwgAuthenticator(
    caCertificate: String,
    private val connectionSource: ConnectionDataSource,
    private val connectionPrefs: ConnectionPrefs,
    // The addKey request must reach the server before that server's own tunnel exists, so its
    // socket needs protect()-ing to bypass the kill-switch's network-lock tunnel — otherwise it's
    // captured and silently dropped, and every WireGuard connection attempt times out.
    protect: (Socket) -> Boolean,
) : WireGuardAuthenticator {
    private val certificatePinningClient = CertificatePinningClientImpl(caCertificate, protect)
    private val client = certificatePinningClient.client()

    override suspend fun authenticate(endpointConfiguration: WireGuardEndpointConfiguration): WireGuardAuthConfiguration {
        val authIp = endpointConfiguration.authIp.asString()

        certificatePinningClient.setKnownEndpointCommonName(
            listOf(authIp to endpointConfiguration.certDn),
        )

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
        val obfuscationSettings = addKeyResponse.obfuscation?.toSettings()
        connectionPrefs.setGateway(addKeyResponse.serverVip)
        connectionPrefs.setAwgObfuscation(obfuscationSettings)
        return WireGuardAuthConfiguration(
            psk = NO_PRESHARED_KEY_BASE64,
            serverPublicKey = addKeyResponse.serverKey,
            clientPrivateKey = keyPair.privateKey.toBase64(),
            internalIp = addKeyResponse.peerIp,
            dnsServers = addKeyResponse.dnsServers,
            gatewayIp = IpAddress.V4(addKeyResponse.serverVip),
            // The addKey response is the source of truth for this attempt's obfuscation params —
            // pass it through so the SDK applies it now instead of only on the next connection.
            obfuscation = obfuscationSettings?.toAmnezia(),
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