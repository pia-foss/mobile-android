package com.kape.login.di

import com.kape.buildconfig.data.BuildConfigProvider
import com.kape.contracts.AuthenticationDataSource
import com.kape.contracts.Router
import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.localprefs.prefs.ConsentPrefs
import com.kape.localprefs.prefs.CsiPrefs
import com.kape.localprefs.prefs.CustomizationPrefs
import com.kape.localprefs.prefs.DipPrefs
import com.kape.localprefs.prefs.KpiPrefs
import com.kape.localprefs.prefs.NetworkManagementPrefs
import com.kape.localprefs.prefs.RatingPrefs
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.localprefs.prefs.ShadowsocksRegionPrefs
import com.kape.localprefs.prefs.VpnRegionPrefs
import com.kape.login.data.AuthenticationDataSourceImpl
import com.kape.login.domain.mobile.GetUserLoggedInUseCase
import com.kape.login.domain.mobile.LoginUseCase
import com.kape.login.domain.mobile.LogoutUseCase
import com.kape.login.domain.tv.LoginUsernameUseCase
import com.kape.login.ui.vm.LoginViewModel
import com.kape.login.ui.vm.mobile.LoginWithEmailViewModel
import com.kape.login.ui.vm.tv.LoginPasswordViewModel
import com.kape.login.ui.vm.tv.LoginUsernameViewModel
import com.kape.login.utils.TokenAuthenticationUtil
import com.kape.payments.SubscriptionPrefs
import com.kape.payments.ui.VpnSubscriptionPaymentProvider
import com.kape.permissions.utils.PermissionUtil
import com.kape.utils.NetworkConnectionListener
import com.kape.vpnconnect.domain.ConnectionUseCase
import com.privateinternetaccess.account.AndroidAccountAPI
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module
class LoginModule {

    @Singleton(binds = [AuthenticationDataSource::class])
    fun provideAuthenticationDataSource(api: AndroidAccountAPI): AuthenticationDataSource =
        AuthenticationDataSourceImpl(api)

    @Singleton
    fun provideLoginUseCase(source: AuthenticationDataSource): LoginUseCase =
        LoginUseCase(source)

    @Singleton
    fun provideLogoutUseCase(
        source: AuthenticationDataSource,
        connectionPrefs: ConnectionPrefs,
        connectionUseCase: ConnectionUseCase,
        csiPrefs: CsiPrefs,
        customizationPrefs: CustomizationPrefs,
        dipPrefs: DipPrefs,
        networkManagementPrefs: NetworkManagementPrefs,
        subscriptionPrefs: SubscriptionPrefs,
        shadowsocksRegionPrefs: ShadowsocksRegionPrefs,
        vpnRegionPrefs: VpnRegionPrefs,
        settingsPrefs: SettingsPrefs,
        kpiPrefs: KpiPrefs,
        consentPrefs: ConsentPrefs,
        ratingPrefs: RatingPrefs,
    ): LogoutUseCase = LogoutUseCase(
        source, connectionPrefs, connectionUseCase, csiPrefs, customizationPrefs, dipPrefs,
        networkManagementPrefs, subscriptionPrefs, shadowsocksRegionPrefs, vpnRegionPrefs,
        settingsPrefs, kpiPrefs, consentPrefs, ratingPrefs,
    )

    @Singleton
    fun provideGetUserLoggedInUseCase(source: AuthenticationDataSource): GetUserLoggedInUseCase =
        GetUserLoggedInUseCase(source)

    @Singleton
    fun provideLoginUsernameUseCase(): LoginUsernameUseCase =
        LoginUsernameUseCase()

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
        vpnSubscriptionPaymentProvider: VpnSubscriptionPaymentProvider,
        buildConfigProvider: BuildConfigProvider,
        permissionsUtil: PermissionUtil,
        networkConnectionListener: NetworkConnectionListener,
    ): LoginViewModel = LoginViewModel(
        router, loginUseCase, vpnSubscriptionPaymentProvider,
        buildConfigProvider, permissionsUtil, networkConnectionListener,
    )

    @KoinViewModel
    fun provideLoginUsernameViewModel(
        router: Router,
        loginUsernameUseCase: LoginUsernameUseCase,
    ): LoginUsernameViewModel = LoginUsernameViewModel(router, loginUsernameUseCase)

    @KoinViewModel
    fun provideLoginPasswordViewModel(
        loginUsernameUseCase: LoginUsernameUseCase,
    ): LoginPasswordViewModel = LoginPasswordViewModel(loginUsernameUseCase)

    @KoinViewModel
    fun provideLoginWithEmailViewModel(
        router: Router,
        useCase: LoginUseCase,
        networkConnectionListener: NetworkConnectionListener,
    ): LoginWithEmailViewModel = LoginWithEmailViewModel(router, useCase, networkConnectionListener)
}