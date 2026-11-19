package com.kape.signup.di

import android.content.Context
import com.kape.payments.di.PaymentsModule
import com.kape.signup.data.YearlyPerMonthFormatter
import com.kape.ui.utils.PriceFormatter
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module(includes = [PaymentsModule::class])
class StoreModule {
    @Singleton
    fun provideYearlyPerMonthFormatter(context: Context): YearlyPerMonthFormatter {
        val formatter = PriceFormatter(context)
        return YearlyPerMonthFormatter { formatter.formatYearlyPerMonth(it.priceInMicros, it.currencyCode, it.formattedPrice) }
    }
}