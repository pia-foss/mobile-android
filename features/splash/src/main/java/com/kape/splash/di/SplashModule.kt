package com.kape.splash.di

import com.kape.contracts.IsUserLoggedInUseCase
import com.kape.contracts.Router
import com.kape.featureflags.domain.ForceUpdateUseCase
import com.kape.httpclient.domain.GetWebsiteDownloadLink
import com.kape.splash.ui.vm.SplashViewModel
import com.kape.utils.DI
import com.kape.utils.PlatformUtils
import com.kape.vpnconnect.domain.ConnectionUseCase
import com.kape.vpnregions.utils.RegionListProvider
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named

@Module
class SplashModule {

    @KoinViewModel
    fun provideSplashViewModel(
        router: Router,
        regionListProvider: RegionListProvider,
        forceUpdateUseCase: ForceUpdateUseCase,
        getWebsiteDownloadLink: GetWebsiteDownloadLink,
        @Named(DI.UPDATE_URL) appUpdateUrl: String,
        connectionUseCase: ConnectionUseCase,
        getUserLoggedInUseCase: IsUserLoggedInUseCase,
        platformUtils: PlatformUtils,
        @Named(DI.IO_DISPATCHER) ioDispatcher: CoroutineDispatcher,
    ): SplashViewModel = SplashViewModel(
        router, regionListProvider, forceUpdateUseCase, getWebsiteDownloadLink,
        appUpdateUrl, connectionUseCase, getUserLoggedInUseCase, platformUtils, ioDispatcher,
    )
}