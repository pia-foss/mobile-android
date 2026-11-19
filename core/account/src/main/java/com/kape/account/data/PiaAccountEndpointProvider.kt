package com.kape.account.data

import com.kape.contracts.MetaEndpointsProvider
import com.privateinternetaccess.account.AccountEndpoint
import com.privateinternetaccess.account.IAccountEndpointProvider
import org.koin.core.annotation.Singleton

private const val ACCOUNT_BASE_ROOT_DOMAIN = "privateinternetaccess.com"
private const val ACCOUNT_PROXY_ROOT_DOMAIN = "piaproxy.net"

@Singleton(binds = [IAccountEndpointProvider::class])
internal class PiaAccountEndpointProvider(
    private val metaEndpoints: MetaEndpointsProvider,
) : IAccountEndpointProvider {
    override fun accountEndpoints(): List<AccountEndpoint> =
        metaEndpoints.metaEndpoints().map {
            AccountEndpoint(it.endpoint, it.isProxy, it.usePinnedCertificate, it.certificateCommonName)
        } +
            listOf(
                AccountEndpoint(ACCOUNT_BASE_ROOT_DOMAIN, isProxy = false, usePinnedCertificate = false, certificateCommonName = null),
                AccountEndpoint(ACCOUNT_PROXY_ROOT_DOMAIN, isProxy = true, usePinnedCertificate = false, certificateCommonName = null),
            )
}