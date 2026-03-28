package com.kape.payments.di

import android.content.Context
import com.kape.payments.SubscriptionPrefs
import com.kape.payments.data.SubscriptionDataSourceImpl
import com.kape.payments.domain.GetPurchaseDetailsUseCase
import com.kape.payments.domain.GetSubscriptionsUseCase
import com.kape.payments.domain.SubscriptionDataSource
import com.kape.payments.ui.DipSubscriptionPaymentProvider
import com.kape.payments.ui.DipSubscriptionPaymentProviderImpl
import com.kape.payments.ui.VpnSubscriptionPaymentProvider
import com.kape.payments.ui.VpnSubscriptionPaymentProviderImpl
import com.privateinternetaccess.account.AndroidAccountAPI
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module
class PaymentsModule {

    @Singleton
    fun provideGetPurchaseDetailsUseCase(prefs: SubscriptionPrefs): GetPurchaseDetailsUseCase =
        GetPurchaseDetailsUseCase(prefs)

    @Singleton(binds = [SubscriptionDataSource::class])
    fun provideSubscriptionDataSource(
        prefs: SubscriptionPrefs,
        api: AndroidAccountAPI,
    ): SubscriptionDataSource = SubscriptionDataSourceImpl(prefs, api)

    @Singleton
    fun provideGetSubscriptionsUseCase(source: SubscriptionDataSource): GetSubscriptionsUseCase =
        GetSubscriptionsUseCase(source)

    @Singleton(binds = [VpnSubscriptionPaymentProvider::class])
    fun provideVpnSubscriptionPaymentProvider(prefs: SubscriptionPrefs): VpnSubscriptionPaymentProvider =
        VpnSubscriptionPaymentProviderImpl(prefs)

    @Singleton(binds = [DipSubscriptionPaymentProvider::class])
    fun provideDipSubscriptionPaymentProvider(context: Context): DipSubscriptionPaymentProvider =
        DipSubscriptionPaymentProviderImpl(context)
}