package com.kape.profile.di

import com.kape.profile.data.CacheDatasource
import com.kape.profile.data.NetworkDatasource
import com.kape.profile.domain.InMemoryCache
import com.kape.profile.domain.NetworkDatasourceImpl
import com.kape.profile.gateways.CacheGateway
import com.kape.profile.gateways.CacheGatewayImpl
import org.koin.dsl.module

val profileModule = module {
    single<NetworkDatasource> { NetworkDatasourceImpl() }
    single<CacheDatasource> { InMemoryCache() }
    single<CacheGateway> { CacheGatewayImpl() }
}