package com.kape.account.data

import com.kape.contracts.MetaEndpointsProvider
import com.kape.data.GenericEndpoint
import com.privateinternetaccess.account.AccountEndpoint
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class PiaAccountEndpointProviderTest {
    private val fallbacks =
        listOf(
            AccountEndpoint("privateinternetaccess.com", isProxy = false, usePinnedCertificate = false, certificateCommonName = null),
            AccountEndpoint("piaproxy.net", isProxy = true, usePinnedCertificate = false, certificateCommonName = null),
        )

    @Test
    fun `tries the meta endpoints first, then the root domains`() {
        val meta =
            MetaEndpointsProvider {
                listOf(
                    GenericEndpoint("10.0.0.1", isProxy = true, usePinnedCertificate = true, certificateCommonName = "meta-1"),
                    GenericEndpoint("10.0.0.2", isProxy = true, usePinnedCertificate = true, certificateCommonName = "meta-2"),
                )
            }

        assertEquals(
            listOf(
                AccountEndpoint("10.0.0.1", isProxy = true, usePinnedCertificate = true, certificateCommonName = "meta-1"),
                AccountEndpoint("10.0.0.2", isProxy = true, usePinnedCertificate = true, certificateCommonName = "meta-2"),
            ) + fallbacks,
            PiaAccountEndpointProvider(meta).accountEndpoints(),
        )
    }

    @Test
    fun `uses only the root domains without meta endpoints`() {
        assertEquals(fallbacks, PiaAccountEndpointProvider { emptyList() }.accountEndpoints())
    }
}