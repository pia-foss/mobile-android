package com.kape.vpn.provider

import com.privateinternetaccess.regions.IRegionEndpointProvider
import com.privateinternetaccess.regions.RegionEndpoint

private const val REGION_BASE_ENDPOINT = "serverlist.piaservers.net"

class RegionsModuleStateProvider(val certificate: String) : IRegionEndpointProvider {

    override fun regionEndpoints(): List<RegionEndpoint> {
        val endpoints = mutableListOf<RegionEndpoint>()
        for (metaEndpoint in metaEndpoints()) {
            endpoints.add(RegionEndpoint(
                metaEndpoint.endpoint,
                metaEndpoint.isProxy,
                metaEndpoint.usePinnedCertificate,
                metaEndpoint.certificateCommonName)
            )
        }
        endpoints.add(
            RegionEndpoint(
                REGION_BASE_ENDPOINT,
                isProxy = false,
                usePinnedCertificate = false,
                certificateCommonName = null
            )
        )
        return endpoints
    }

    private fun metaEndpoints(): List<GenericEndpoint> {
        // TODO: implement
        return mutableListOf()
    }
}