package com.kape.payments.di

import com.kape.payments.data.SubscriptionDataSourceImpl
import com.kape.payments.domain.GetPurchaseDetailsUseCase
import com.kape.payments.domain.GetSubscriptionsUseCase
import com.kape.payments.domain.SubscriptionDataSource
import com.kape.payments.ui.PaymentProvider
import com.kape.payments.ui.PaymentProviderImpl
import com.kape.payments.utils.SubscriptionPrefs
import org.koin.core.module.Module
import org.koin.dsl.module

fun paymentsModule(appModule: Module) = module {
    includes(appModule, localPaymentsModule)
}

private val localPaymentsModule = module {
    single { SubscriptionPrefs(get()) }
    single<SubscriptionDataSource> { SubscriptionDataSourceImpl(get(), get()) }
    single { GetSubscriptionsUseCase(get()) }
    single { GetPurchaseDetailsUseCase(get()) }
    single<PaymentProvider> { PaymentProviderImpl(get()) }
}