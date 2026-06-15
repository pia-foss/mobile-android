package com.kape.sidemenu.di

import com.kape.contracts.AppInfo
import com.kape.contracts.LogoutUseCase
import com.kape.contracts.Router
import com.kape.data.DI
import com.kape.profile.domain.GetProfileUseCase
import com.kape.sidemenu.ui.vm.SideMenuViewModel
import com.kape.utils.UpdateAvailableManager
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named

@Module
class SideMenuModule {
    @KoinViewModel
    fun provideSideMenuViewModel(
        profileUseCase: GetProfileUseCase,
        logoutUseCase: LogoutUseCase,
        appInfo: AppInfo,
        router: Router,
        updateAvailableManager: UpdateAvailableManager,
        @Named(DI.IO_DISPATCHER) ioDispatcher: CoroutineDispatcher,
    ) = SideMenuViewModel(
        profileUseCase,
        logoutUseCase,
        appInfo,
        router,
        updateAvailableManager,
        ioDispatcher,
    )
}