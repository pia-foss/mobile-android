package com.kape.permissions.domain

import org.koin.core.annotation.Singleton

@Singleton
class IsVpnProfileInstalledUseCase(
    private val dataSource: VpnPermissionDataSource,
) {
    fun isVpnProfileInstalled(): Boolean = dataSource.isVpnProfileInstalled()
}