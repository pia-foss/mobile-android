package com.kape.automation.di

import com.kape.automation.ui.AutomationViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

fun automationModule(appModule: Module) = module {
    includes(appModule, localAutomationModule)
}

private val localAutomationModule = module {
    viewModel { AutomationViewModel(get()) }
}