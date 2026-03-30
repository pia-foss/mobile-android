package com.kape.permissions.utils

import androidx.navigation.NavController
import com.kape.contracts.data.ComposeDestination
import com.kape.contracts.data.Connection
import com.kape.contracts.data.NotificationPermission
import com.kape.contracts.data.VpnPermission
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