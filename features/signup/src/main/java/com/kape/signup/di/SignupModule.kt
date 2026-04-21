package com.kape.signup.di

import android.content.Context
import com.kape.buildconfig.data.BuildConfigProvider
import com.kape.contracts.Router
import com.kape.localprefs.prefs.ConsentPrefs
import com.kape.login.domain.mobile.LoginUseCase
import com.kape.payments.SubscriptionPrefs
import com.kape.payments.domain.GetPurchaseDetailsUseCase
import com.kape.payments.domain.GetSubscriptionsUseCase
import com.kape.payments.ui.VpnSubscriptionPaymentProvider
import com.kape.permissions.utils.PermissionUtil
import com.kape.signup.data.EmailDataSourceImpl
import com.kape.signup.data.Identifier
import com.kape.signup.data.IdentifierImpl
import com.kape.signup.data.Obfuscator
import com.kape.signup.data.ObfuscatorImpl
import com.kape.signup.data.SignupDataSourceImpl
import com.kape.signup.domain.ConsentUseCase
import com.kape.signup.domain.EmailDataSource
import com.kape.signup.domain.GetObfuscatedDeviceIdentifierUseCase
import com.kape.signup.domain.SetEmailUseCase
import com.kape.signup.domain.SignupDataSource
import com.kape.signup.domain.SignupUseCase
import com.kape.signup.ui.vm.SignupViewModel
import com.kape.ui.utils.PriceFormatter
import com.kape.utils.NetworkConnectionListener
import com.privateinternetaccess.account.AndroidAccountAPI
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module
class SignupModule {
    @Singleton(binds = [EmailDataSource::class])
    fun provideEmailDataSource(api: AndroidAccountAPI): EmailDataSource = EmailDataSourceImpl(api)

    @Singleton(binds = [Identifier::class])
    fun provideIdentifier(context: Context): Identifier = IdentifierImpl(context)

    @Singleton(binds = [Obfuscator::class])
    fun provideObfuscator(): Obfuscator = ObfuscatorImpl()

    @Singleton
    fun provideConsentUseCase(prefs: ConsentPrefs): ConsentUseCase = ConsentUseCase(prefs)

    @Singleton
    fun provideGetObfuscatedDeviceIdentifierUseCase(
        identifier: Identifier,
        obfuscator: Obfuscator,
    ): GetObfuscatedDeviceIdentifierUseCase = GetObfuscatedDeviceIdentifierUseCase(identifier, obfuscator)

    @Singleton
    fun provideSetEmailUseCase(source: EmailDataSource): SetEmailUseCase = SetEmailUseCase(source)

    @Singleton(binds = [SignupDataSource::class])
    fun provideSignupDataSource(api: AndroidAccountAPI): SignupDataSource = SignupDataSourceImpl(api)

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

    @KoinViewModel
    fun provideSignupViewModel(
        router: Router,
        vpnSubscriptionPaymentProvider: VpnSubscriptionPaymentProvider,
        formatter: PriceFormatter,
        consentUseCase: ConsentUseCase,
        useCase: SignupUseCase,
        subscriptionPrefs: SubscriptionPrefs,
        subscriptionsUseCase: GetSubscriptionsUseCase,
        buildConfigProvider: BuildConfigProvider,
        permissionUtil: PermissionUtil,
        networkConnectionListener: NetworkConnectionListener,
    ): SignupViewModel =
        SignupViewModel(
            router,
            vpnSubscriptionPaymentProvider,
            formatter,
            consentUseCase,
            useCase,
            subscriptionPrefs,
            subscriptionsUseCase,
            buildConfigProvider,
            permissionUtil,
            networkConnectionListener,
        )
}