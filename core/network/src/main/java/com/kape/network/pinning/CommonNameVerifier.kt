package com.kape.network.pinning

import com.kape.network.pinning.EndpointIdentity
import org.spongycastle.asn1.x500.X500Name
import org.spongycastle.asn1.x500.style.BCStyle
import javax.security.auth.x500.X500Principal

internal class CommonNameVerifier(
    private val knownEndpoints: List<EndpointIdentity>,
) {
    fun matches(
        endpoint: String,
        commonName: String?,
    ): Boolean =
        commonName != null &&
            knownEndpoints.any { it.endpoint == endpoint && it.commonName == commonName }
}

internal fun commonNameOf(principal: X500Principal): String? =
    X500Name
        .getInstance(principal.encoded)
        .getRDNs(BCStyle.CN)
        .firstOrNull()
        ?.first
        ?.value
        ?.toString()