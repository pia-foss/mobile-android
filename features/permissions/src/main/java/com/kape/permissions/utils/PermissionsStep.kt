package com.kape.permissions.utils

sealed class PermissionsStep {
    data object Vpn : PermissionsStep()
    data object Notifications : PermissionsStep()
}