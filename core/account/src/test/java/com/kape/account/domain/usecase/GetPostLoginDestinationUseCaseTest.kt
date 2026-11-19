package com.kape.account.domain.usecase

import com.kape.contracts.PermissionStatus
import com.kape.data.Connection
import com.kape.data.NotificationPermission
import com.kape.data.VpnPermission
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class GetPostLoginDestinationUseCaseTest {
    private fun destination(
        vpnProfileInstalled: Boolean,
        notificationsAllowed: Boolean,
    ) = GetPostLoginDestinationUseCase(
        object : PermissionStatus {
            override fun isVpnProfileInstalled() = vpnProfileInstalled

            override fun areNotificationsAllowed() = notificationsAllowed
        },
    )()

    @Test
    fun `asks for the VPN permission first`() {
        assertEquals(VpnPermission, destination(vpnProfileInstalled = false, notificationsAllowed = false))
        assertEquals(VpnPermission, destination(vpnProfileInstalled = false, notificationsAllowed = true))
    }

    @Test
    fun `asks for notifications once the VPN profile is installed`() {
        assertEquals(NotificationPermission, destination(vpnProfileInstalled = true, notificationsAllowed = false))
    }

    @Test
    fun `goes to the connection screen when both are granted`() {
        assertEquals(Connection, destination(vpnProfileInstalled = true, notificationsAllowed = true))
    }
}