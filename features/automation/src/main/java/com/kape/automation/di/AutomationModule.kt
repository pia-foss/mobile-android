package com.kape.automation.di

import com.kape.automation.ui.viewmodel.AutomationViewModel
import com.kape.location.data.LocationPermissionManager
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

fun automationModule(appModule: Module) = module {
    includes(appModule, localAutomationModule)
}

private val localAutomationModule = module {
    single { LocationPermissionManager(get()) }
    viewModel { AutomationViewModel(get(), get(), get()) }
}