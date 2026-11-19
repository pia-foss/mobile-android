package com.kape.contracts

interface PermissionStatus {
    fun isVpnProfileInstalled(): Boolean

    fun areNotificationsAllowed(): Boolean
}