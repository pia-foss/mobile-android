package com.kape.vpn_permissions.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.router.ExitFlow
import com.kape.router.Router
import com.kape.vpn_permissions.domain.IsVpnProfileInstalledUseCase
import com.kape.vpn_permissions.utils.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class PermissionsViewModel(
    private val useCaseIsVpnProfileInstalled: IsVpnProfileInstalledUseCase,
) : ViewModel(), KoinComponent {

    private val router: Router by inject()
    private val _vpnPermissionState = MutableStateFlow(IDLE)
    val vpnPermissionState: StateFlow<VpnProfileState> = _vpnPermissionState

    fun checkFlowCompleted() {
        if (useCaseIsVpnProfileInstalled.isVpnProfileInstalled()) {
            router.handleFlow(ExitFlow.VpnPermission)
        }
    }

    fun onOkButtonClicked() {
        viewModelScope.launch {
            if (useCaseIsVpnProfileInstalled.isVpnProfileInstalled()) {
                router.handleFlow(ExitFlow.VpnPermission)
                return@launch
            }
            _vpnPermissionState.emit(REQUEST)
        }
    }

    fun onVpnProfileStateChange() {
        viewModelScope.launch {
            if (useCaseIsVpnProfileInstalled.isVpnProfileInstalled()) {
                _vpnPermissionState.emit(GRANTED)
                router.handleFlow(ExitFlow.VpnPermission)
                return@launch
            }
            _vpnPermissionState.emit(NOT_GRANTED)
        }
    }
}