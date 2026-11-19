package com.kape.regions.di

import com.kape.contracts.di.Qualifiers
import com.privateinternetaccess.regions.IRegionEndpointProvider
import com.privateinternetaccess.regions.PlatformInstancesProvider
import com.privateinternetaccess.regions.RegionsAPI
import com.privateinternetaccess.regions.RegionsBuilder
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Singleton

@Module
@ComponentScan("com.kape.regions")
class RegionsModule {
    @Singleton
    fun provideRegionsApi(
        endpointProvider: IRegionEndpointProvider,
        platformProvider: PlatformInstancesProvider,
        @Named(Qualifiers.CERTIFICATE) certificate: String,
        @Named(Qualifiers.USER_AGENT) userAgent: String,
    ): RegionsAPI =
        RegionsBuilder()
            .setEndpointProvider(endpointProvider)
            .setCertificate(certificate)
            .setUserAgent(userAgent)
            .setMetadataRequestPath("/vpninfo/regions/v2")
            .setVpnRegionsRequestPath("/vpninfo/servers/v7")
            .setShadowsocksRegionsRequestPath("/shadow_socks")
            .setPlatformInstancesProvider(platformProvider)
            .build()
}