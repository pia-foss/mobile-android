package com.kape.dedicatedip.di

import com.kape.dedicatedip.domain.DipPurchaseDataSource
import com.kape.dedicatedip.domain.NoOpDipPurchaseDataSource
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module
class DipPurchaseModule {
    @Singleton(binds = [DipPurchaseDataSource::class])
    fun provideDipPurchaseDataSource(): DipPurchaseDataSource = NoOpDipPurchaseDataSource()
}