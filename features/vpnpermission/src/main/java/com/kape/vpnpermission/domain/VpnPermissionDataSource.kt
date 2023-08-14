package com.kape.vpnpermission.domain

interface VpnPermissionDataSource {
    fun isVpnProfileInstalled(): Boolean
}