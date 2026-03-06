package com.kape.permissions.di

import com.kape.notifications.data.NotificationPermissionManager
import com.kape.permissions.data.VpnPermissionDataSourceImpl
import com.kape.permissions.domain.IsVpnProfileInstalledUseCase
import com.kape.permissions.domain.VpnPermissionDataSource
import com.kape.permissions.ui.vm.PermissionsViewModel
import com.kape.permissions.utils.PermissionUtil
import com.kape.router.NotificationPermission
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

fun permissionsModule(appModule: Module) = module {
    includes(appModule, localPermissionModule)
}

val localPermissionModule = module {
    single<VpnPermissionDataSource> { VpnPermissionDataSourceImpl(context = get()) }
    single { IsVpnProfileInstalledUseCase(dataSource = get()) }
    single { NotificationPermissionManager(get()) }
    single { PermissionUtil(get(), get()) }
    viewModelOf(::PermissionsViewModel)
}