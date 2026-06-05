package com.kape.signup.di

import com.kape.login.domain.mobile.LoginUseCase
import com.kape.payments.domain.GetPurchaseDetailsUseCase
import com.kape.payments.domain.GetSubscriptionsUseCase
import com.kape.payments.prefs.SubscriptionPrefs
import com.kape.payments.ui.VpnSubscriptionPaymentProvider
import com.kape.signup.data.GoogleSignupBillingHandler
import com.kape.signup.data.SignupHandlerImpl
import com.kape.signup.domain.EmailDataSource
import com.kape.signup.domain.GetObfuscatedDeviceIdentifierUseCase
import com.kape.signup.domain.SignupBillingHandler
import com.kape.signup.domain.SignupDataSource
import com.kape.signup.domain.SignupHandler
import com.kape.signup.domain.SignupUseCase
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

    @Singleton
    fun provideSignupUseCase(
        signupDataSource: SignupDataSource,
        loginUseCase: LoginUseCase,
        emailDataSource: EmailDataSource,
        purchaseDetailsUseCase: GetPurchaseDetailsUseCase,
        getObfuscatedDeviceIdentifierUseCase: GetObfuscatedDeviceIdentifierUseCase,
    ): SignupUseCase =
        SignupUseCase(
            signupDataSource,
            loginUseCase,
            emailDataSource,
            purchaseDetailsUseCase,
            getObfuscatedDeviceIdentifierUseCase,
        )

    @Singleton(binds = [SignupHandler::class])
    fun provideSignupHandler(useCase: SignupUseCase): SignupHandler = SignupHandlerImpl(useCase)
}