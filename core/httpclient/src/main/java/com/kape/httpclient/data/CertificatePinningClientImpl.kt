package com.kape.httpclient.data

import com.kape.httpclient.domain.CertificatePinningClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import org.spongycastle.asn1.x500.X500Name
import org.spongycastle.asn1.x500.style.BCStyle
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.security.InvalidKeyException
import java.security.KeyManagementException
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.NoSuchProviderException
import java.security.SecureRandom
import java.security.SignatureException
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.Arrays
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLPeerUnverifiedException
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager
import javax.security.auth.x500.X500Principal

class CertificatePinningClientImpl(private val certificate: String) : CertificatePinningClient {

    private lateinit var knownEndpointCommonName: List<Pair<String, String>>

    override fun client(): HttpClient {
        var trustManager: X509TrustManager? = null
        var sslSocketFactory: SSLSocketFactory? = null
        try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            val inputStream = certificate.byteInputStream()
            val certificateFactory = CertificateFactory.getInstance("X.509")
            val certificate = certificateFactory.generateCertificate(inputStream)
            keyStore.setCertificateEntry("pia", certificate)
            inputStream.close()
            val trustManagerFactory =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            trustManagerFactory.init(keyStore)
            val trustManagers = trustManagerFactory.trustManagers
            check(!(trustManagers.size != 1 || trustManagers[0] !is X509TrustManager)) {
                "Unexpected default trust managers:" + Arrays.toString(trustManagers)
            }
            trustManager = trustManagers[0] as X509TrustManager
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustManagers, SecureRandom())
            sslSocketFactory = sslContext.socketFactory
        } catch (e: KeyStoreException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: CertificateException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: KeyManagementException) {
            e.printStackTrace()
        }

        val client = HttpClient(OkHttp) {
            engine {
                config {
                    if (trustManager != null && sslSocketFactory != null) {
                        sslSocketFactory(sslSocketFactory, trustManager)
                    }

                    hostnameVerifier { endpoint, session ->
                        var verified = false
                        try {
                            val x509CertificateChain =
                                session.peerCertificates as Array<out X509Certificate>
                            trustManager?.checkServerTrusted(x509CertificateChain, "RSA")
                            val sessionCertificate = session.peerCertificates.first()
                            verified =
                                verifyCommonName(endpoint, sessionCertificate as X509Certificate)
                        } catch (e: SSLPeerUnverifiedException) {
                            e.printStackTrace()
                        } catch (e: CertificateException) {
                            e.printStackTrace()
                        } catch (e: InvalidKeyException) {
                            e.printStackTrace()
                        } catch (e: NoSuchAlgorithmException) {
                            e.printStackTrace()
                        } catch (e: NoSuchProviderException) {
                            e.printStackTrace()
                        } catch (e: SignatureException) {
                            e.printStackTrace()
                        }
                        verified
                    }
                }
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 8000
            }
        }
        return client
    }

    override fun setKnownEndpointCommonName(knownEndpointCommonName: List<Pair<String, String>>) {
        this.knownEndpointCommonName = knownEndpointCommonName
    }

    private fun verifyCommonName(
        requestEndpoint: String,
        certificate: X509Certificate,
    ): Boolean {
        val principal = certificate.subjectDN as X500Principal
        certificateCommonName(X500Name.getInstance(principal.encoded))?.let { certCommonName ->
            for ((endpoint, commonName) in knownEndpointCommonName) {
                if (isEqual(endpoint.toByteArray(), requestEndpoint.toByteArray()) &&
                    isEqual(commonName.toByteArray(), certCommonName.toByteArray())
                ) {
                    return true
                }
            }
        }
        return false
    }

    private fun certificateCommonName(name: X500Name): String? {
        val rdns = name.getRDNs(BCStyle.CN)
        return if (rdns.isEmpty()) {
            null
        } else {
            rdns.first().first.value.toString()
        }
    }

    private fun isEqual(a: ByteArray, b: ByteArray): Boolean {
        val messageDigest = MessageDigest.getInstance("SHA-256")
        val random = SecureRandom()
        val randomBytes = ByteArray(20)
        random.nextBytes(randomBytes)

        val concatA = ByteArrayOutputStream()
        concatA.write(randomBytes)
        concatA.write(a)
        val digestA = messageDigest.digest(concatA.toByteArray())

        val concatB = ByteArrayOutputStream()
        concatB.write(randomBytes)
        concatB.write(b)
        val digestB = messageDigest.digest(concatB.toByteArray())

        return MessageDigest.isEqual(digestA, digestB)
    }
}