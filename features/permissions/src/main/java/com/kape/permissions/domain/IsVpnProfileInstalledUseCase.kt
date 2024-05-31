package com.kape.permissions.domain

class IsVpnProfileInstalledUseCase(
    private val dataSource: VpnPermissionDataSource,
) {
    fun isVpnProfileInstalled(): Boolean = dataSource.isVpnProfileInstalled()
}