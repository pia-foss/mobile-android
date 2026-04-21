package com.kape.permissions.di

import android.content.Context
import com.kape.contracts.Router
import com.kape.notifications.data.NotificationPermissionManager
import com.kape.permissions.data.VpnPermissionDataSourceImpl
import com.kape.permissions.domain.IsVpnProfileInstalledUseCase
import com.kape.permissions.domain.VpnPermissionDataSource
import com.kape.permissions.ui.vm.PermissionsViewModel
import com.kape.permissions.utils.PermissionUtil
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module
class PermissionsModule {
    @Singleton(binds = [VpnPermissionDataSource::class])
    fun provideVpnPermissionDataSource(context: Context): VpnPermissionDataSource = VpnPermissionDataSourceImpl(context)

    @Singleton
    fun provideIsVpnProfileInstalledUseCase(dataSource: VpnPermissionDataSource): IsVpnProfileInstalledUseCase =
        IsVpnProfileInstalledUseCase(dataSource)

    @Singleton
    fun providePermissionUtil(
        useCaseIsVpnProfileInstalled: IsVpnProfileInstalledUseCase,
        notificationPermissionManager: NotificationPermissionManager,
    ): PermissionUtil = PermissionUtil(useCaseIsVpnProfileInstalled, notificationPermissionManager)

    @KoinViewModel
    fun providePermissionsViewModel(
        permissionUtil: PermissionUtil,
        router: Router,
    ): PermissionsViewModel = PermissionsViewModel(permissionUtil, router)
}