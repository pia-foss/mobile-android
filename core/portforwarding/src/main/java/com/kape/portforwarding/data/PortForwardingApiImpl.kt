package com.kape.portforwarding.data

import android.net.Uri
import com.kape.connection.model.PortBindInformation
import com.kape.httpclient.domain.CertificatePinningClient
import com.kape.portforwarding.domain.PortForwardingApi
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Singleton
import java.io.IOException

@Singleton([PortForwardingApi::class])
class PortForwardingApiImpl(
    private val certificatePinningClient: CertificatePinningClient,
    private val userAgent: String,
) : PortForwardingApi {

    private val client = certificatePinningClient.client()

    @Throws(IOException::class)
    override suspend fun getPayloadAndSignature(
        vpnToken: String,
        gateway: String,
    ): PortBindInformation? {
        val urlEncodedEndpoint: String = Uri.parse("https://$gateway:19999/getSignature")
            .buildUpon()
            .appendQueryParameter("token", vpnToken)
            .build().toString()
        return try {
            val response = client.get(urlEncodedEndpoint) {
                header("User-Agent", userAgent)
            }
            if (response.status != HttpStatusCode.OK) {
                return null
            }
            val jsonString = response.body<String>()
            val portBindingInfo = Json.decodeFromString<PortBindInformation>(jsonString)
            if (portBindingInfo.status != "OK") {
                null
            } else {
                portBindingInfo
            }
        } catch (exception: Exception) {
            null
        }
    }

    @Throws(IOException::class, IllegalStateException::class)
    override suspend fun bindPort(
        token: String,
        payload: String,
        signature: String,
        endpoint: String,
    ): Boolean {
        val urlEncodedEndpoint: String = Uri.parse("https://$endpoint:19999/bindPort")
            .buildUpon()
            .appendQueryParameter("payload", payload)
            .appendQueryParameter("signature", signature)
            .build().toString()
        return try {
            val response = client.get(urlEncodedEndpoint) {
                header(HttpHeaders.UserAgent, userAgent)
                header(HttpHeaders.Authorization, token)
            }
            response.status == HttpStatusCode.OK
        } catch (exception: Exception) {
            false
        }
    }

    override fun setKnownEndpointCommonName(knownEndpointCommonName: List<Pair<String, String>>) {
        certificatePinningClient.setKnownEndpointCommonName(knownEndpointCommonName)
    }
}