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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import java.io.IOException

class PortForwardingApiImpl(
    private val certificatePinningClient: CertificatePinningClient,
    private val userAgent: String,
) : PortForwardingApi {

    private val client = certificatePinningClient.client()

    @Throws(IOException::class)
    override fun getPayloadAndSignature(
        vpnToken: String,
        gateway: String,
    ): Flow<PortBindInformation?> = flow {
        val urlEncodedEndpoint: String = Uri.parse("https://$gateway:19999/getSignature")
            .buildUpon()
            .appendQueryParameter("token", vpnToken)
            .build().toString()
        try {
            val response = client.get(urlEncodedEndpoint) {
                header("User-Agent", userAgent)
            }
            if (response.status != HttpStatusCode.OK) {
                emit(null)
            }
            val jsonString = response.body<String>()
            val portBindingInfo = Json.decodeFromString<PortBindInformation>(jsonString)
            if (portBindingInfo.status != "OK") {
                emit(null)
            } else {
                emit(portBindingInfo)
            }
        } catch (exception: Exception) {
            emit(null)
        }
    }

    @Throws(IOException::class, IllegalStateException::class)
    override fun bindPort(
        token: String,
        payload: String,
        signature: String,
        endpoint: String,
    ): Flow<Boolean> = flow {
        val urlEncodedEndpoint: String = Uri.parse("https://$endpoint:19999/bindPort")
            .buildUpon()
            .appendQueryParameter("payload", payload)
            .appendQueryParameter("signature", signature)
            .build().toString()
        try {
            val response = client.get(urlEncodedEndpoint) {
                header(HttpHeaders.UserAgent, userAgent)
                header(HttpHeaders.Authorization, token)
            }
            emit(response.status == HttpStatusCode.OK)
        } catch (exception: Exception) {
            emit(false)
        }
    }

    override fun setKnownEndpointCommonName(knownEndpointCommonName: List<Pair<String, String>>) {
        certificatePinningClient.setKnownEndpointCommonName(knownEndpointCommonName)
    }
}