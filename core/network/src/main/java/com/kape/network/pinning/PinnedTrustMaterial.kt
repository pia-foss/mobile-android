package com.kape.network.pinning

import java.security.GeneralSecurityException
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.CertificateFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

internal class PinnedTrustMaterial(
    val trustManager: X509TrustManager,
    val sslSocketFactory: SSLSocketFactory,
) {
    companion object {
        // Fails closed: throws rather than falling back to the system trust store, which would
        // silently disable pinning.
        fun fromPem(
            certificatePem: String,
            keyStoreType: String = "AndroidKeyStore",
        ): PinnedTrustMaterial =
            try {
                val pinnedCertificate =
                    certificatePem.byteInputStream().use {
                        CertificateFactory.getInstance("X.509").generateCertificate(it)
                    }
                val keyStore =
                    KeyStore.getInstance(keyStoreType).apply {
                        load(null)
                        setCertificateEntry("pia", pinnedCertificate)
                    }
                val trustManagers =
                    TrustManagerFactory
                        .getInstance(TrustManagerFactory.getDefaultAlgorithm())
                        .apply { init(keyStore) }
                        .trustManagers
                val trustManager =
                    trustManagers.singleOrNull() as? X509TrustManager
                        ?: error("Unexpected default trust managers: ${trustManagers.contentToString()}")
                val sslContext =
                    SSLContext.getInstance("TLS").apply {
                        init(null, arrayOf(trustManager), SecureRandom())
                    }
                PinnedTrustMaterial(trustManager, sslContext.socketFactory)
            } catch (e: GeneralSecurityException) {
                throw IllegalStateException("Certificate pinning setup failed", e)
            } catch (e: java.io.IOException) {
                throw IllegalStateException("Certificate pinning setup failed", e)
            }
    }
}