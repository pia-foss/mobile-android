package com.kape.dedicatedip.di

import android.content.Context
import com.kape.payments.di.PaymentsModule
import com.kape.ui.utils.PriceFormatter
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module(includes = [PaymentsModule::class])
class DipStoreModule {
    @Singleton
    fun providePriceFormatter(context: Context): PriceFormatter = PriceFormatter(context)
}