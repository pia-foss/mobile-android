package com.kape.vpnpermission.domain

class IsVpnProfileInstalledUseCase(
    private val dataSource: VpnPermissionDataSource,
) {
    fun isVpnProfileInstalled(): Boolean = dataSource.isVpnProfileInstalled()
}