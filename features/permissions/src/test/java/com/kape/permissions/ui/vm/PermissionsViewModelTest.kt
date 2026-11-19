package com.kape.permissions.ui.vm

import com.kape.contracts.PermissionStatus
import com.kape.contracts.Router
import com.kape.data.ComposeDestination
import com.kape.data.Connection
import com.kape.data.NotificationPermission
import com.kape.permissions.utils.GRANTED
import com.kape.permissions.utils.NOT_GRANTED
import com.kape.permissions.utils.REQUEST
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class PermissionsViewModelTest {
    private val permissions = FakePermissionStatus()
    private val router = RecordingRouter()
    private val viewModel = PermissionsViewModel(permissions, router)

    @Test
    fun `ok button requests the VPN permission`() {
        viewModel.onOkButtonClicked()

        assertEquals(REQUEST, viewModel.vpnPermissionState.value)
    }

    @Test
    fun `stays on the screen when the VPN permission is denied`() {
        viewModel.onVpnProfileStateChange()

        assertEquals(NOT_GRANTED, viewModel.vpnPermissionState.value)
        assertEquals(emptyList<ComposeDestination>(), router.destinations)
    }

    @Test
    fun `asks for notifications next when they are not allowed`() {
        permissions.vpn = true

        viewModel.onVpnProfileStateChange()

        assertEquals(GRANTED, viewModel.vpnPermissionState.value)
        assertEquals(listOf(NotificationPermission), router.destinations)
    }

    @Test
    fun `goes to the connection screen when notifications are already allowed`() {
        permissions.vpn = true
        permissions.notifications = true

        viewModel.onVpnProfileStateChange()

        assertEquals(listOf(Connection), router.destinations)
    }
}

private class FakePermissionStatus : PermissionStatus {
    var vpn = false
    var notifications = false

    override fun isVpnProfileInstalled() = vpn

    override fun areNotificationsAllowed() = notifications
}

private class RecordingRouter : Router {
    val destinations = mutableListOf<ComposeDestination>()

    override fun updateDestination(destination: ComposeDestination) {
        destinations += destination
    }

    override fun getNavigationState(): StateFlow<ComposeDestination?> = MutableStateFlow(null)

    override fun resetNavigation() = Unit

    override fun navigateBack() = Unit

    override fun getBackState(): StateFlow<Boolean> = MutableStateFlow(false)

    override fun resetBack() = Unit
}