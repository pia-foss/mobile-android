package com.kape.vpn.provider

import com.privateinternetaccess.account.AccountEndpoint
import com.privateinternetaccess.account.IAccountEndpointProvider

private const val ACCOUNT_BASE_ROOT_DOMAIN = "privateinternetaccess.com"
private const val ACCOUNT_PROXY_ROOT_DOMAIN = "piaproxy.net"

class AccountModuleStateProvider(val certificate: String) : IAccountEndpointProvider {
    override fun accountEndpoints(): List<AccountEndpoint> {
        val endpoints = mutableListOf<AccountEndpoint>()
        for (metaEndpoint in metaEndpoints()) {
            endpoints.add(
                AccountEndpoint(
                    metaEndpoint.endpoint,
                    metaEndpoint.isProxy,
                    metaEndpoint.usePinnedCertificate,
                    metaEndpoint.certificateCommonName
                )
            )
        }
        endpoints.add(
            AccountEndpoint(
                ACCOUNT_BASE_ROOT_DOMAIN,
                isProxy = false,
                usePinnedCertificate = false,
                certificateCommonName = null
            )
        )
        endpoints.add(
            AccountEndpoint(
                ACCOUNT_PROXY_ROOT_DOMAIN,
                isProxy = true,
                usePinnedCertificate = false,
                certificateCommonName = null
            )
        )

//        if (PiaPrefHandler.useStaging(context)) {
//            val stagingHost = if (PiaPrefHandler.getStagingServer(context).isNullOrEmpty()) {
//                BuildConfig.STAGEINGHOST
//            } else {
//                PiaPrefHandler.getStagingServer(context)
//            }
//            endpoints.clear()
//            endpoints.add(
//                AccountEndpoint(
//                    stagingHost.replace("https://", "").replace("http://", ""),
//                    isProxy = false,
//                    usePinnedCertificate = false,
//                    certificateCommonName = null
//                )
//            )
//        }
        return endpoints
    }

    private fun metaEndpoints(): List<GenericEndpoint> {
        // TODO: implement
        return mutableListOf()
    }



}