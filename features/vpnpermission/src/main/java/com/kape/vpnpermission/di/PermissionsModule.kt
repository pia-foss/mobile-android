package com.kape.vpnpermission.di

import com.kape.vpnpermission.data.VpnPermissionDataSourceImpl
import com.kape.vpnpermission.domain.IsVpnProfileInstalledUseCase
import com.kape.vpnpermission.domain.VpnPermissionDataSource
import com.kape.vpnpermission.ui.vm.PermissionsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val permissionModule = module {
    single<VpnPermissionDataSource> { VpnPermissionDataSourceImpl(context = get()) }
    single { IsVpnProfileInstalledUseCase(dataSource = get()) }
    viewModel {
        PermissionsViewModel(get(), get())
    }
}