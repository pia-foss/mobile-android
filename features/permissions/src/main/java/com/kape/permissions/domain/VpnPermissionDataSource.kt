package com.kape.permissions.domain

interface VpnPermissionDataSource {
    fun isVpnProfileInstalled(): Boolean
}