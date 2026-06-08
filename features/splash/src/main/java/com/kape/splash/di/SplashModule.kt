package com.kape.splash.di

import com.kape.contracts.ConnectionInfoProvider
import com.kape.contracts.ConnectionManager
import com.kape.contracts.IsUserLoggedInUseCase
import com.kape.contracts.Router
import com.kape.data.DI
import com.kape.featureflags.domain.ForceUpdateUseCase
import com.kape.splash.data.LatestAppVersionDataSourceImpl
import com.kape.splash.domain.GetAppLatestVersionUseCase
import com.kape.splash.domain.LatestAppVersionDataSource
import com.kape.splash.ui.vm.SplashViewModel
import com.kape.utils.PlatformUtils
import com.kape.vpnregions.utils.RegionListProvider
import com.privateinternetaccess.account.AndroidAccountAPI
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Singleton

@Module
class SplashModule {
    @Singleton(binds = [LatestAppVersionDataSource::class])
    fun provideLatestAppVersionDataSource(api: AndroidAccountAPI): LatestAppVersionDataSource = LatestAppVersionDataSourceImpl(api)

    @Singleton
    fun provideGetAppLatestVersionUseCase(dataSource: LatestAppVersionDataSource) = GetAppLatestVersionUseCase(dataSource)

    @KoinViewModel
    fun provideSplashViewModel(
        router: Router,
        regionListProvider: RegionListProvider,
        forceUpdateUseCase: ForceUpdateUseCase,
        getAppLatestVersionUseCase: GetAppLatestVersionUseCase,
        @Named(DI.UPDATE_URL) appUpdateUrl: String,
        connectionManager: ConnectionManager,
        connectionInfoProvider: ConnectionInfoProvider,
        isUserLoggedIn: IsUserLoggedInUseCase,
        platformUtils: PlatformUtils,
        @Named(DI.IO_DISPATCHER) ioDispatcher: CoroutineDispatcher,
    ): SplashViewModel =
        SplashViewModel(
            router,
            regionListProvider,
            forceUpdateUseCase,
            getAppLatestVersionUseCase,
            appUpdateUrl,
            connectionManager,
            connectionInfoProvider,
            isUserLoggedIn,
            platformUtils,
            ioDispatcher,
        )
}