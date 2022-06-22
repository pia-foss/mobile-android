package com.kape.vpn_permissions.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.router.ExitFlow
import com.kape.router.Router
import com.kape.vpn_permissions.domain.IsVpnProfileInstalledUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class PermissionsViewModel(
        private val useCaseIsVpnProfileInstalled: IsVpnProfileInstalledUseCase
) : ViewModel(), KoinComponent {

    private val router: Router by inject()
    private val _showState = MutableStateFlow(ShowState.IDLE)
    val showState: StateFlow<ShowState> = _showState

    fun onScreenLaunch() {
        viewModelScope.launch {
            if (useCaseIsVpnProfileInstalled.isVpnProfileInstalled()) {
                router.handleFlow(ExitFlow.VpnPermission)
                return@launch
            }
            _showState.emit(ShowState.IDLE)
        }
    }

    fun onOkButtonClicked() {
        viewModelScope.launch {
            if (useCaseIsVpnProfileInstalled.isVpnProfileInstalled()) {
                router.handleFlow(ExitFlow.VpnPermission)
                return@launch
            }
            _showState.emit(ShowState.REQUEST_VPN_PROFILE)
        }
    }

    fun onVpnProfileStateChange() {
        viewModelScope.launch {
            if (useCaseIsVpnProfileInstalled.isVpnProfileInstalled()) {
                router.handleFlow(ExitFlow.VpnPermission)
                return@launch
            }
            _showState.emit(ShowState.SHOW_VPN_PROFILE_NOT_GRANTED_TOAST)
        }
    }

    enum class ShowState {
        IDLE,
        REQUEST_VPN_PROFILE,
        SHOW_VPN_PROFILE_NOT_GRANTED_TOAST
    }
}