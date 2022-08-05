package com.kape.payments.di

import com.kape.payments.data.SubscriptionDataSourceImpl
import com.kape.payments.domain.BillingDataSource
import com.kape.payments.domain.GetPurchaseDetailsUseCase
import com.kape.payments.domain.GetSubscriptionsUseCase
import com.kape.payments.domain.SubscriptionDataSource
import com.kape.payments.ui.BillingDataSourceImpl
import com.kape.payments.utils.SubscriptionPrefs
import org.koin.dsl.module

val paymentsModule = module {
    single { SubscriptionPrefs(get()) }
    single<SubscriptionDataSource> { SubscriptionDataSourceImpl(get()) }
    single { GetSubscriptionsUseCase(get()) }
    single { GetPurchaseDetailsUseCase(get()) }
    single<BillingDataSource> { BillingDataSourceImpl(get()) }
}