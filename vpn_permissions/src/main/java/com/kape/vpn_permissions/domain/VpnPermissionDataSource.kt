package com.kape.vpn_permissions.domain

interface VpnPermissionDataSource {
    fun isVpnProfileInstalled(): Boolean
}
