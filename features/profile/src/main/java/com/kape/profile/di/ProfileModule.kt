package com.kape.profile.di

import com.kape.contracts.Router
import com.kape.login.domain.mobile.LogoutUseCase
import com.kape.profile.data.ProfileDatasourceImpl
import com.kape.profile.domain.DeleteAccountUseCase
import com.kape.profile.domain.GetProfileUseCase
import com.kape.profile.domain.ProfileDatasource
import com.kape.profile.ui.vm.ProfileViewModel
import com.privateinternetaccess.account.AndroidAccountAPI
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module
class ProfileModule {

    @Singleton(binds = [ProfileDatasource::class])
    fun provideProfileDatasource(api: AndroidAccountAPI): ProfileDatasource =
        ProfileDatasourceImpl(api)

    @Singleton
    fun provideGetProfileUseCase(profile: ProfileDatasource): GetProfileUseCase =
        GetProfileUseCase(profile)

    @Singleton
    fun provideDeleteAccountUseCase(profile: ProfileDatasource): DeleteAccountUseCase =
        DeleteAccountUseCase(profile)

    @KoinViewModel
    fun provideProfileViewModel(
        useCase: GetProfileUseCase,
        deleteAccountUseCase: DeleteAccountUseCase,
        logoutUseCase: LogoutUseCase,
        router: Router,
    ): ProfileViewModel = ProfileViewModel(useCase, deleteAccountUseCase, logoutUseCase, router)
}