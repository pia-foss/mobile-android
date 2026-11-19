package com.kape.account.domain.usecase

import com.kape.contracts.PermissionStatus
import com.kape.data.ComposeDestination
import com.kape.data.Connection
import com.kape.data.NotificationPermission
import com.kape.data.VpnPermission
import org.koin.core.annotation.Factory

@Factory
class GetPostLoginDestinationUseCase(
    private val permissions: PermissionStatus,
) {
    operator fun invoke(): ComposeDestination =
        when {
            !permissions.isVpnProfileInstalled() -> VpnPermission
            !permissions.areNotificationsAllowed() -> NotificationPermission
            else -> Connection
        }
}