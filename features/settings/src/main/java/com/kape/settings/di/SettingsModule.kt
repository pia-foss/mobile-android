package com.kape.settings.di

import com.kape.settings.ui.SettingsViewModel
import com.kape.settings.utils.SettingsPrefs
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

fun settingsModule(appModule: Module) = module {
    includes(appModule, localSettingsModule)
}

private val localSettingsModule = module {
    single { SettingsPrefs(get()) }
    viewModel { SettingsViewModel(get(), get()) }
}