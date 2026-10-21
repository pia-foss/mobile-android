package com.kape.vpn.di

import android.content.Context
import com.kape.location.mocklocation.FusedLocationMocker
import com.kape.vpn.location.GmsFusedLocationMocker
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module
class FlavorLocationModule {
    @Singleton(binds = [FusedLocationMocker::class])
    fun provideFusedLocationMocker(context: Context): FusedLocationMocker = GmsFusedLocationMocker(context)
}