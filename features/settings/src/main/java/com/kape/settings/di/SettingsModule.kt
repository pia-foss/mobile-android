package com.kape.settings.di

import com.kape.settings.domain.IsNumericIpAddressUseCase
import com.kape.settings.domain.IsNumericIpAddressUseCaseImpl
import com.kape.settings.ui.vm.SettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

fun settingsModule(appModule: Module, versionCode: Int, versionName: String) = module {
    includes(appModule, localSettingsModule("$versionName ($versionCode)"))
}

private fun localSettingsModule(version: String) = module {
    single<IsNumericIpAddressUseCase> { IsNumericIpAddressUseCaseImpl() }
    viewModel { SettingsViewModel(get(), get(), get(), version, get(), get(), get(), get()) }
}