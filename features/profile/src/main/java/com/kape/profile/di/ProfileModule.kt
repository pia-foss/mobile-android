package com.kape.profile.di

import com.kape.profile.data.ProfileDatasourceImpl
import com.kape.profile.domain.GetProfileUseCase
import com.kape.profile.domain.ProfileDatasource
import com.kape.profile.ui.vm.ProfileViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

fun profileModule(appModule: Module) = module {
    includes(appModule, localProfileModule)
}

private val localProfileModule = module {
    single<ProfileDatasource> { ProfileDatasourceImpl(get()) }
    single { GetProfileUseCase(get()) }
    viewModel { ProfileViewModel(get(), get()) }
}