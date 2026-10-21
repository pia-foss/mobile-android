package com.kape.vpn.di

import com.kape.location.mocklocation.FusedLocationMocker
import com.kape.location.mocklocation.NoOpFusedLocationMocker
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module
class FlavorLocationModule {
    @Singleton(binds = [FusedLocationMocker::class])
    fun provideFusedLocationMocker(): FusedLocationMocker = NoOpFusedLocationMocker()
}