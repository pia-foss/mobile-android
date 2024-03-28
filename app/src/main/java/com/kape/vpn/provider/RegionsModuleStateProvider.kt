package com.kape.vpn.provider

import com.privateinternetaccess.regions.IRegionEndpointProvider
import com.privateinternetaccess.regions.RegionEndpoint

private const val REGION_BASE_ENDPOINT = "serverlist.piaservers.net"

class RegionsModuleStateProvider(
    val certificate: String,
    private val metaEndpointsProvider: MetaEndpointsProvider,
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
        return endpoints
    }
}