package com.kape.signup.di

import com.kape.payments.domain.GetSubscriptionsUseCase
import com.kape.payments.prefs.SubscriptionPrefs
import com.kape.payments.ui.VpnSubscriptionPaymentProvider
import com.kape.signup.data.GoogleSignupBillingHandler
import com.kape.signup.domain.SignupBillingHandler
import com.kape.ui.utils.PriceFormatter
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module
class SignupBillingModule {
    @Singleton(binds = [SignupBillingHandler::class])
    fun provideSignupBillingHandler(
        vpnSubscriptionPaymentProvider: VpnSubscriptionPaymentProvider,
        subscriptionPrefs: SubscriptionPrefs,
        subscriptionsUseCase: GetSubscriptionsUseCase,
        formatter: PriceFormatter,
    ): SignupBillingHandler =
        GoogleSignupBillingHandler(
            vpnSubscriptionPaymentProvider,
            subscriptionPrefs,
            subscriptionsUseCase,
            formatter,
        )
}