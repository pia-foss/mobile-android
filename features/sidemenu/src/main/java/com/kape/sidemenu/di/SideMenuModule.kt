package com.kape.sidemenu.di

import com.kape.contracts.AppInfo
import com.kape.contracts.LogoutUseCase
import com.kape.contracts.Router
import com.kape.profile.domain.GetProfileUseCase
import com.kape.sidemenu.ui.vm.SideMenuViewModel
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Module

@Module
class SideMenuModule {
    @KoinViewModel
    fun provideSideMenuViewModel(
        profileUseCase: GetProfileUseCase,
        logoutUseCase: LogoutUseCase,
        appInfo: AppInfo,
        router: Router,
    ) = SideMenuViewModel(profileUseCase, logoutUseCase, appInfo, router)
}