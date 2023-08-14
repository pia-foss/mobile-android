package com.kape.sidemenu.di

import com.kape.sidemenu.ui.vm.SideMenuViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

fun sideMenuModule(versionCode: Int, versionName: String) = module {
    viewModel {
        SideMenuViewModel(get(), get(), versionCode, versionName)
    }
}