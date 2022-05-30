package com.kape.vpn_permissions.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.vpn_permissions.domain.IsVpnProfileInstalledUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PermissionsViewModel(
        private val useCaseIsVpnProfileInstalled: IsVpnProfileInstalledUseCase
) : ViewModel() {

    private val _showState = MutableStateFlow(ShowState.START)
    val showState: StateFlow<ShowState> = _showState

    fun resetShowState() {
        viewModelScope.launch {
            _showState.emit(ShowState.IDLE)
        }
    }

    fun onScreenLaunch() {
        viewModelScope.launch {
            val nextState: ShowState = if (useCaseIsVpnProfileInstalled.isVpnProfileInstalled()) ShowState.SHOW_VPN_PROFILE_GRANTED_TOAST else ShowState.IDLE
            _showState.emit(nextState)
        }
    }

    fun onOkButtonClicked() {
        viewModelScope.launch {
            if (useCaseIsVpnProfileInstalled.isVpnProfileInstalled()) {
                _showState.emit(ShowState.SHOW_VPN_PROFILE_GRANTED_TOAST)
                return@launch
            }

            _showState.emit(ShowState.REQUEST_VPN_PROFILE)
        }
    }

    fun onVpnProfileStateChange() {
        viewModelScope.launch {
            _showState.emit(if (useCaseIsVpnProfileInstalled.isVpnProfileInstalled()) ShowState.SHOW_VPN_PROFILE_GRANTED_TOAST else ShowState.SHOW_VPN_PROFILE_NOT_GRANTED_TOAST)
        }
    }

    enum class ShowState {
        START,
        IDLE,
        REQUEST_VPN_PROFILE,
        SHOW_VPN_PROFILE_GRANTED_TOAST,
        SHOW_VPN_PROFILE_NOT_GRANTED_TOAST,
    }
}