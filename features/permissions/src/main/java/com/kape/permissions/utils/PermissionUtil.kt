package com.kape.permissions.utils

import androidx.navigation.NavController
import com.kape.notifications.data.NotificationPermissionManager
import com.kape.permissions.domain.IsVpnProfileInstalledUseCase
import com.kape.router.ComposeDestination
import com.kape.router.Connection
import com.kape.router.NotificationPermission
import com.kape.router.VpnPermission

class PermissionUtil(
    private val useCaseIsVpnProfileInstalled: IsVpnProfileInstalledUseCase,
    private val notificationPermissionManager: NotificationPermissionManager,
) {
    fun isNotificationPermissionGranted(): Boolean =
        notificationPermissionManager.isNotificationsPermissionGranted()

    fun isVpnProfileInstalled(): Boolean = useCaseIsVpnProfileInstalled.isVpnProfileInstalled()

    fun getNextDestination(): ComposeDestination {
        return if (!useCaseIsVpnProfileInstalled.isVpnProfileInstalled()) {
            VpnPermission
        } else if (!notificationPermissionManager.isNotificationsPermissionGranted()) {
            NotificationPermission
        } else {
            Connection
        }
    }
}