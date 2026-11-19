package com.kape.location.di

import com.kape.location.mocklocation.FusedLocationMocker
import com.kape.location.mocklocation.NoOpFusedLocationMocker
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module
class FusedLocationModule {
    @Singleton(binds = [FusedLocationMocker::class])
    fun provideFusedLocationMocker(): NoOpFusedLocationMocker = NoOpFusedLocationMocker()
}