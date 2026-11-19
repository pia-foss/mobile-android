package com.kape.permissions.ui.vm

import androidx.lifecycle.ViewModel
import com.kape.contracts.PermissionStatus
import com.kape.contracts.Router
import com.kape.data.Connection
import com.kape.data.NotificationPermission
import com.kape.permissions.utils.GRANTED
import com.kape.permissions.utils.IDLE
import com.kape.permissions.utils.NOT_GRANTED
import com.kape.permissions.utils.REQUEST
import com.kape.permissions.utils.VpnProfileState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class PermissionsViewModel(
    private val permissions: PermissionStatus,
    private val router: Router,
) : ViewModel() {
    private val _vpnPermissionState = MutableStateFlow(IDLE)
    val vpnPermissionState: StateFlow<VpnProfileState> = _vpnPermissionState.asStateFlow()

    fun onOkButtonClicked() {
        _vpnPermissionState.value = REQUEST
    }

    fun onVpnProfileStateChange() {
        if (!permissions.isVpnProfileInstalled()) {
            _vpnPermissionState.value = NOT_GRANTED
            return
        }
        _vpnPermissionState.value = GRANTED
        router.updateDestination(if (permissions.areNotificationsAllowed()) Connection else NotificationPermission)
    }

    fun navigateToConnection() = router.updateDestination(Connection)
}