package com.kape.login.di

import com.kape.buildconfig.data.BuildConfigProvider
import com.kape.contracts.AppInfo
import com.kape.contracts.AuthenticationDataSource
import com.kape.contracts.ConnectionManager
import com.kape.contracts.DeviceInfo
import com.kape.contracts.IsUserLoggedInUseCase
import com.kape.contracts.LogoutUseCase
import com.kape.contracts.Router
import com.kape.data.DI
import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.localprefs.prefs.FeaturePrefs
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.login.data.AuthenticationDataSourceImpl
import com.kape.login.domain.LogoutHandler
import com.kape.login.domain.mobile.GetUserLoggedInUseCase
import com.kape.login.domain.mobile.LoginFailureTracker
import com.kape.login.domain.mobile.LoginUseCase
import com.kape.login.domain.mobile.LoginWithReceiptHandler
import com.kape.login.domain.mobile.LogoutUseCaseImpl
import com.kape.login.domain.tv.LoginUsernameUseCase
import com.kape.login.ui.vm.LoginViewModel
import com.kape.login.ui.vm.mobile.LoginWithEmailViewModel
import com.kape.login.ui.vm.tv.LoginPasswordViewModel
import com.kape.login.ui.vm.tv.LoginUsernameViewModel
import com.kape.login.utils.TokenAuthenticationUtil
import com.kape.permissions.utils.PermissionUtil
import com.kape.shareevents.data.KpiEventGenerator
import com.kape.shareevents.domain.SubmitKpiEventUseCase
import com.kape.utils.NetworkConnectionListener
import com.privateinternetaccess.account.AndroidAccountAPI
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Singleton

@Module
class LoginModule {
    @Singleton(binds = [AuthenticationDataSource::class])
    fun provideAuthenticationDataSource(
        api: AndroidAccountAPI,
        featurePrefs: FeaturePrefs,
    ): AuthenticationDataSource = AuthenticationDataSourceImpl(api, featurePrefs)

    @Singleton
    fun provideLoginUseCase(source: AuthenticationDataSource): LoginUseCase = LoginUseCase(source)

    @Singleton
    fun provideLogoutUseCase(
        source: AuthenticationDataSource,
        connectionPrefs: ConnectionPrefs,
        settingsPrefs: SettingsPrefs,
        connectionManager: ConnectionManager,
        logoutHandler: LogoutHandler,
    ): LogoutUseCase =
        LogoutUseCaseImpl(
            source,
            connectionPrefs,
            settingsPrefs,
            connectionManager,
            logoutHandler,
        )

    @Singleton
    fun provideGetUserLoggedInUseCase(source: AuthenticationDataSource): IsUserLoggedInUseCase = GetUserLoggedInUseCase(source)

    @Singleton
    fun provideLoginUsernameUseCase(): LoginUsernameUseCase = LoginUsernameUseCase()

    @Singleton
    fun provideLoginFailureTracker(): LoginFailureTracker = LoginFailureTracker()

    @Singleton
    fun provideTokenAuthenticationUtil(
        dataSource: AuthenticationDataSource,
        router: Router,
        permissionUtil: PermissionUtil,
    ): TokenAuthenticationUtil = TokenAuthenticationUtil(dataSource, router, permissionUtil)

    @KoinViewModel
    fun provideLoginViewModel(
        router: Router,
        loginUseCase: LoginUseCase,
        loginWithReceiptHandler: LoginWithReceiptHandler,
        buildConfigProvider: BuildConfigProvider,
        permissionsUtil: PermissionUtil,
        submitKpiEventUseCase: SubmitKpiEventUseCase,
        appInfo: AppInfo,
        deviceInfo: DeviceInfo,
        loginFailureTracker: LoginFailureTracker,
        featurePrefs: FeaturePrefs,
        eventGenerator: KpiEventGenerator,
        @Named(DI.IO_DISPATCHER) ioDispatcher: CoroutineDispatcher,
        networkConnectionListener: NetworkConnectionListener,
    ): LoginViewModel =
        LoginViewModel(
            router,
            loginUseCase,
            loginWithReceiptHandler,
            buildConfigProvider,
            permissionsUtil,
            submitKpiEventUseCase,
            appInfo,
            deviceInfo,
            loginFailureTracker,
            featurePrefs,
            eventGenerator,
            ioDispatcher,
            networkConnectionListener,
        )

    @KoinViewModel
    fun provideLoginUsernameViewModel(
        router: Router,
        loginUsernameUseCase: LoginUsernameUseCase,
    ): LoginUsernameViewModel = LoginUsernameViewModel(router, loginUsernameUseCase)

    @KoinViewModel
    fun provideLoginPasswordViewModel(loginUsernameUseCase: LoginUsernameUseCase): LoginPasswordViewModel =
        LoginPasswordViewModel(loginUsernameUseCase)

    @KoinViewModel
    fun provideLoginWithEmailViewModel(
        router: Router,
        useCase: LoginUseCase,
        networkConnectionListener: NetworkConnectionListener,
        @Named(DI.IO_DISPATCHER) ioDispatcher: CoroutineDispatcher,
    ): LoginWithEmailViewModel = LoginWithEmailViewModel(router, useCase, networkConnectionListener)
}