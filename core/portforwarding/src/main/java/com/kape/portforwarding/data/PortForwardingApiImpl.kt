package com.kape.portforwarding.data

import android.net.Uri
import android.util.Base64
import com.kape.connection.model.DecodedPayload
import com.kape.httpclient.domain.CertificatePinningClient
import com.kape.portforwarding.data.model.PayloadAndSignature
import com.kape.portforwarding.domain.PortForwardingApi
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.json.JSONObject
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
    ): Flow<PayloadAndSignature?> = flow {
        var payload: DecodedPayload? = null
        var signature = ""

        val urlEncodedEndpoint: String = Uri.parse("https://$gateway:19999/getSignature")
            .buildUpon()
            .appendQueryParameter("token", vpnToken)
            .build().toString()
        val response = client.get(urlEncodedEndpoint) {
            header("User-Agent", userAgent)
        }
        if (response.status != HttpStatusCode.OK) {
            emit(null)
        }
        val jsonString = response.body<String>()
        val jsonResponse = JSONObject(jsonString)
        if (!jsonResponse.has("status") || jsonResponse["status"] != "OK") {
            emit(null)
        }
        if (jsonResponse.has("payload")) {
            val data = jsonResponse["payload"].toString()
            payload = decodePayload(data)
        }
        if (jsonResponse.has("signature")) {
            signature = jsonResponse["signature"].toString()
        }
        payload?.let {
            val payloadAndSignatureResponse = PayloadAndSignature(it, signature)
            emit(payloadAndSignatureResponse)
        } ?: run {
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
        val response = client.get(urlEncodedEndpoint) {
            header(HttpHeaders.UserAgent, userAgent)
            header(HttpHeaders.Authorization, token)
        }
        emit(response.status == HttpStatusCode.OK)
    }

    override fun setKnownEndpointCommonName(knownEndpointCommonName: List<Pair<String, String>>) {
        certificatePinningClient.setKnownEndpointCommonName(knownEndpointCommonName)
    }

    private fun decodePayload(payload: String): DecodedPayload {
        var token = ""
        var port = 0
        var expirationDate = ""
        val decodedString = String(Base64.decode(payload, Base64.DEFAULT))
        val json = JSONObject(decodedString)
        if (json.has("token")) {
            token = json["token"].toString()
        }
        if (json.has("port")) {
            port = json.optInt("port")
        }
        if (json.has("expires_at")) {
            expirationDate = json["expires_at"].toString()
        }
        return DecodedPayload(port, token, expirationDate)
    }
}