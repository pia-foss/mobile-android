package com.kape.inappbrowser.di

import com.kape.inappbrowser.ui.InAppBrowserViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

fun inAppBrowserModule(appModule: Module) = module {
    includes(appModule, localInAppBrowserModule)
}

private val localInAppBrowserModule = module {
    viewModel { InAppBrowserViewModel(get()) }
}