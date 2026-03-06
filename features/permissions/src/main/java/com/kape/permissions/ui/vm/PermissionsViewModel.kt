package com.kape.permissions.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.permissions.utils.GRANTED
import com.kape.permissions.utils.IDLE
import com.kape.permissions.utils.NOT_GRANTED
import com.kape.permissions.utils.PermissionUtil
import com.kape.permissions.utils.REQUEST
import com.kape.permissions.utils.VpnProfileState
import com.kape.router.ComposeDestination
import com.kape.router.NotificationPermission
import com.kape.router.Router
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

class PermissionsViewModel(
    private val permissionUtil: PermissionUtil,
    val router: Router,
) : ViewModel(), KoinComponent {

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
}