package com.kape.network.pinning

import android.util.Log
import com.kape.contracts.di.Qualifiers
import com.kape.network.pinning.EndpointIdentity
import com.kape.network.pinning.PinnedHttpClientFactory
import com.kape.network.pinning.SocketProtector
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import org.koin.core.annotation.Named
import org.koin.core.annotation.Singleton
import java.security.cert.X509Certificate
import javax.net.ssl.SSLPeerUnverifiedException
import javax.net.ssl.SSLSession

@Singleton(binds = [PinnedHttpClientFactory::class])
internal class PinnedHttpClientFactoryImpl(
    @Named(Qualifiers.CERTIFICATE) private val certificatePem: String,
) : PinnedHttpClientFactory {
    private val trustMaterial by lazy { PinnedTrustMaterial.fromPem(certificatePem) }

    override fun create(
        knownEndpoints: List<EndpointIdentity>,
        socketProtector: SocketProtector?,
    ): HttpClient {
        val trust = trustMaterial
        val commonNameVerifier = CommonNameVerifier(knownEndpoints.toList())

        return HttpClient(OkHttp) {
            engine {
                config {
                    // The handshake validates the chain against the pinned certificate through
                    // this socket factory, so the hostname verifier only has to check the CN.
                    sslSocketFactory(trust.sslSocketFactory, trust.trustManager)
                    hostnameVerifier { endpoint, session ->
                        verifyCommonName(commonNameVerifier, endpoint, session)
                    }
                    socketProtector?.let { socketFactory(ProtectingSocketFactory(it)) }
                }
            }
            install(HttpTimeout) {
                requestTimeoutMillis = REQUEST_TIMEOUT_MS
            }
        }
    }

    private fun verifyCommonName(
        verifier: CommonNameVerifier,
        endpoint: String,
        session: SSLSession,
    ): Boolean {
        val leaf =
            try {
                session.peerCertificates.firstOrNull() as? X509Certificate
            } catch (e: SSLPeerUnverifiedException) {
                Log.w(TAG, "Rejecting $endpoint: peer not verified", e)
                return false
            }
        val commonName = leaf?.let { commonNameOf(it.subjectX500Principal) }
        val matches = verifier.matches(endpoint, commonName)
        if (!matches) Log.w(TAG, "Rejecting $endpoint: unexpected certificate common name $commonName")
        return matches
    }

    companion object {
        private const val REQUEST_TIMEOUT_MS = 8_000L
        private const val TAG = "PinnedHttpClient"
    }
}