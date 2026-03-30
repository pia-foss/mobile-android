package com.kape.permissions.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.contracts.Router
import com.kape.contracts.data.Connection
import com.kape.contracts.data.NotificationPermission
import com.kape.permissions.utils.GRANTED
import com.kape.permissions.utils.IDLE
import com.kape.permissions.utils.NOT_GRANTED
import com.kape.permissions.utils.PermissionUtil
import com.kape.permissions.utils.REQUEST
import com.kape.permissions.utils.VpnProfileState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import org.koin.core.component.KoinComponent

@KoinViewModel
class PermissionsViewModel(
    private val permissionUtil: PermissionUtil,
    private val router: Router,
) : ViewModel(){

    private val _vpnPermissionState = MutableStateFlow(IDLE)
    val vpnPermissionState: StateFlow<VpnProfileState> = _vpnPermissionState

    fun onOkButtonClicked() = viewModelScope.launch {
        _vpnPermissionState.emit(REQUEST)
    }

    fun onVpnProfileStateChange() =
        viewModelScope.launch {
            if (permissionUtil.isVpnProfileInstalled()) {
                _vpnPermissionState.emit(GRANTED)
                router.updateDestination(NotificationPermission)
                return@launch
            }
            _vpnPermissionState.emit(NOT_GRANTED)
        }

    fun navigateToConnection() = router.updateDestination(Connection)
}