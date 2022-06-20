package com.kape.sidemenu.di

import com.kape.sidemenu.ui.vm.SideMenuViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val sideMenuModule = module {
    viewModel {
        SideMenuViewModel()
    }
}
