package com.kape.signup.di

import android.content.Context
import com.kape.contracts.Router
import com.kape.data.DI
import com.kape.localprefs.prefs.ConsentPrefs
import com.kape.login.domain.mobile.LoginUseCase
import com.kape.payments.domain.GetPurchaseDetailsUseCase
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
import com.kape.signup.domain.SignupBillingHandler
import com.kape.signup.domain.SignupDataSource
import com.kape.signup.domain.SignupUseCase
import com.kape.signup.ui.vm.SignupViewModel
import com.kape.utils.NetworkConnectionListener
import com.privateinternetaccess.account.AndroidAccountAPI
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
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
        billingHandler: SignupBillingHandler,
        consentUseCase: ConsentUseCase,
        useCase: SignupUseCase,
        permissionUtil: PermissionUtil,
        networkConnectionListener: NetworkConnectionListener,
        @Named(DI.IO_DISPATCHER) ioDispatcher: CoroutineDispatcher,
    ): SignupViewModel =
        SignupViewModel(
            router,
            billingHandler,
            consentUseCase,
            useCase,
            permissionUtil,
            ioDispatcher,
            networkConnectionListener,
        )
}