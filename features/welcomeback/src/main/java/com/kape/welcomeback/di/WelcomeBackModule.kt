package com.kape.welcomeback.di

import com.kape.contracts.Router
import com.kape.data.DI
import com.kape.login.domain.mobile.LoginUseCase
import com.kape.login.domain.mobile.LoginWithReceiptHandler
import com.kape.permissions.utils.PermissionUtil
import com.kape.shareevents.data.KpiEventGenerator
import com.kape.shareevents.domain.SubmitKpiEventUseCase
import com.kape.utils.PlatformUtils
import com.kape.welcomeback.ui.vm.WelcomeBackViewModel
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named

@Module
class WelcomeBackModule {
    @KoinViewModel
    fun provideWelcomeBackViewModel(
        router: Router,
        loginUseCase: LoginUseCase,
        loginWithReceiptHandler: LoginWithReceiptHandler,
        permissionsUtil: PermissionUtil,
        submitKpiEventUseCase: SubmitKpiEventUseCase,
        eventGenerator: KpiEventGenerator,
        platformUtils: PlatformUtils,
        @Named(DI.IO_DISPATCHER) ioDispatcher: CoroutineDispatcher,
    ): WelcomeBackViewModel =
        WelcomeBackViewModel(
            router,
            loginUseCase,
            loginWithReceiptHandler,
            permissionsUtil,
            submitKpiEventUseCase,
            eventGenerator,
            platformUtils,
            ioDispatcher,
        )
}