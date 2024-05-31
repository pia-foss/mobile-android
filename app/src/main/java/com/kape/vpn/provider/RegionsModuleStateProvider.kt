package com.kape.vpn.provider

import com.kape.vpn.utils.STAGING
import com.privateinternetaccess.regions.IRegionEndpointProvider
import com.privateinternetaccess.regions.RegionEndpoint

private const val REGION_BASE_ENDPOINT = "serverlist.piaservers.net"

class RegionsModuleStateProvider(
    val certificate: String,
    private val metaEndpointsProvider: MetaEndpointsProvider,
    private val useStaging: Boolean,
) : IRegionEndpointProvider {

    override fun regionEndpoints(): List<RegionEndpoint> {
        val endpoints = mutableListOf<RegionEndpoint>()
        for (metaEndpoint in metaEndpointsProvider.metaEndpoints()) {
            endpoints.add(
                RegionEndpoint(
                    metaEndpoint.endpoint,
                    metaEndpoint.isProxy,
                    metaEndpoint.usePinnedCertificate,
                    metaEndpoint.certificateCommonName,
                ),
            )
        }
        endpoints.add(
            RegionEndpoint(
                REGION_BASE_ENDPOINT,
                isProxy = false,
                usePinnedCertificate = false,
                certificateCommonName = null,
            ),
        )

        if (useStaging) {
            endpoints.clear()
            endpoints.add(
                RegionEndpoint(
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