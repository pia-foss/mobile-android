package com.kape.vpn_permissions.di

import com.kape.vpn_permissions.data.VpnPermissionDataSourceImpl
import com.kape.vpn_permissions.domain.*
import com.kape.vpn_permissions.ui.vm.PermissionsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val permissionModule = module {
    single<VpnPermissionDataSource> { VpnPermissionDataSourceImpl(context = get()) }
    single { IsVpnProfileInstalledUseCase(dataSource = get()) }
    viewModel {
        PermissionsViewModel(get())
    }
}
