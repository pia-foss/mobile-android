package com.kape.payments.di

import com.kape.payments.SubscriptionPrefs
import com.kape.payments.data.SubscriptionDataSourceImpl
import com.kape.payments.domain.GetPurchaseDetailsUseCase
import com.kape.payments.domain.GetSubscriptionsUseCase
import com.kape.payments.domain.SubscriptionDataSource
import com.kape.payments.ui.DipSubscriptionPaymentProvider
import com.kape.payments.ui.DipSubscriptionPaymentProviderImpl
import com.kape.payments.ui.VpnSubscriptionPaymentProvider
import com.kape.payments.ui.VpnSubscriptionPaymentProviderImpl
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
    single<VpnSubscriptionPaymentProvider> { VpnSubscriptionPaymentProviderImpl(get()) }
    single<DipSubscriptionPaymentProvider> { DipSubscriptionPaymentProviderImpl(get()) }
}