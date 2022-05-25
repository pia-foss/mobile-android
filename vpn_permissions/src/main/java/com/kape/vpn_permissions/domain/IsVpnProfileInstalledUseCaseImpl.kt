package com.kape.vpn_permissions.domain

class IsVpnProfileInstalledUseCase(
        private val dataSource: VpnPermissionDataSource
) {
    fun isVpnProfileInstalled(): Boolean = dataSource.isVpnProfileInstalled()
}
