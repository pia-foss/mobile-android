package com.kape.vpn.provider

import com.privateinternetaccess.account.AccountEndpoint
import com.privateinternetaccess.account.IAccountEndpointProvider

private const val STAGING = "https://staging-3-77b8e3a311bcb6ec5e96.privateinternetaccess.com"

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