package com.kape.vpn.provider

import com.kape.vpn.utils.STAGING
import com.privateinternetaccess.account.AccountEndpoint
import com.privateinternetaccess.account.IAccountEndpointProvider

class AccountModuleStateProvider(
    val certificate: String,
    private val metaEndpointsProvider: MetaEndpointsProvider,
    private val useStaging: Boolean,
) : IAccountEndpointProvider {
    override fun accountEndpoints(): List<AccountEndpoint> {
        val endpoints = mutableListOf<AccountEndpoint>()
        for (metaEndpoint in metaEndpointsProvider.metaEndpoints()) {
            endpoints.add(
                AccountEndpoint(
                    metaEndpoint.endpoint,
                    metaEndpoint.isProxy,
                    metaEndpoint.usePinnedCertificate,
                    metaEndpoint.certificateCommonName,
                ),
            )
        }
        endpoints.add(
            AccountEndpoint(
                ACCOUNT_BASE_ROOT_DOMAIN,
                isProxy = false,
                usePinnedCertificate = false,
                certificateCommonName = null,
            ),
        )
        endpoints.add(
            AccountEndpoint(
                ACCOUNT_PROXY_ROOT_DOMAIN,
                isProxy = true,
                usePinnedCertificate = false,
                certificateCommonName = null,
            ),
        )

        if (useStaging) {
            endpoints.clear()
            endpoints.add(
                AccountEndpoint(
                    STAGING.replace("https://", "").replace("http://", ""),
                    isProxy = false,
                    usePinnedCertificate = false,
                    certificateCommonName = null,
                ),
            )
        }
        return endpoints
    }
}