package com.kape.payments.di

import android.content.Context
import com.kape.data.DI
import com.kape.payments.data.SubscriptionDataSourceImpl
import com.kape.payments.domain.GetPurchaseDetailsUseCase
import com.kape.payments.domain.GetSubscriptionsUseCase
import com.kape.payments.domain.SubscriptionDataSource
import com.kape.payments.prefs.SubscriptionPrefs
import com.kape.payments.ui.DipSubscriptionPaymentProvider
import com.kape.payments.ui.DipSubscriptionPaymentProviderImpl
import com.kape.payments.ui.VpnSubscriptionPaymentProvider
import com.kape.payments.ui.VpnSubscriptionPaymentProviderImpl
import com.privateinternetaccess.account.AndroidAccountAPI
import kotlinx.coroutines.CoroutineScope
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Singleton

@Module
class PaymentsModule {
    @Singleton
    fun provideGetPurchaseDetailsUseCase(prefs: SubscriptionPrefs): GetPurchaseDetailsUseCase = GetPurchaseDetailsUseCase(prefs)

    @Singleton(binds = [SubscriptionDataSource::class])
    fun provideSubscriptionDataSource(
        prefs: SubscriptionPrefs,
        api: AndroidAccountAPI,
        @Named(DI.IO_SCOPE) ioScope: CoroutineScope,
    ): SubscriptionDataSource = SubscriptionDataSourceImpl(prefs, api, ioScope)

    @Singleton
    fun provideGetSubscriptionsUseCase(source: SubscriptionDataSource): GetSubscriptionsUseCase = GetSubscriptionsUseCase(source)

    @Singleton(binds = [VpnSubscriptionPaymentProvider::class])
    fun provideVpnSubscriptionPaymentProvider(
        prefs: SubscriptionPrefs,
        @Named(DI.IO_SCOPE) ioScope: CoroutineScope,
    ): VpnSubscriptionPaymentProvider = VpnSubscriptionPaymentProviderImpl(prefs, ioScope = ioScope)

    @Singleton(binds = [DipSubscriptionPaymentProvider::class])
    fun provideDipSubscriptionPaymentProvider(context: Context): DipSubscriptionPaymentProvider =
        DipSubscriptionPaymentProviderImpl(context)

    @Singleton
    fun provideSubscriptionPrefs(context: Context): SubscriptionPrefs = SubscriptionPrefs(context)
}