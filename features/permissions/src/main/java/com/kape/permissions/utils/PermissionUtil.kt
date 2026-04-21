package com.kape.permissions.utils

import com.kape.data.ComposeDestination
import com.kape.data.Connection
import com.kape.data.NotificationPermission
import com.kape.data.VpnPermission
import com.kape.notifications.data.NotificationPermissionManager
import com.kape.permissions.domain.IsVpnProfileInstalledUseCase
import org.koin.core.annotation.Singleton

@Singleton
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