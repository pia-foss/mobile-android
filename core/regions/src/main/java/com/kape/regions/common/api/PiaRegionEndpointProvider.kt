package com.kape.regions.common.api

import com.kape.contracts.MetaEndpointsProvider
import com.privateinternetaccess.regions.IRegionEndpointProvider
import com.privateinternetaccess.regions.RegionEndpoint
import org.koin.core.annotation.Singleton

private const val REGION_BASE_ENDPOINT = "serverlist.piaservers.net"

@Singleton(binds = [IRegionEndpointProvider::class])
internal class PiaRegionEndpointProvider(
    private val metaEndpoints: MetaEndpointsProvider,
) : IRegionEndpointProvider {
    override fun regionEndpoints(): List<RegionEndpoint> =
        metaEndpoints.metaEndpoints().map {
            RegionEndpoint(it.endpoint, it.isProxy, it.usePinnedCertificate, it.certificateCommonName)
        } +
            RegionEndpoint(
                endpoint = REGION_BASE_ENDPOINT,
                isProxy = false,
                usePinnedCertificate = false,
                certificateCommonName = null,
            )
}