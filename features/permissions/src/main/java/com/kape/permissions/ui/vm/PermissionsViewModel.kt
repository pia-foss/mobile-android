package com.kape.permissions.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.notifications.data.NotificationPermissionManager
import com.kape.router.ExitFlow
import com.kape.router.Router
import com.kape.permissions.domain.IsVpnProfileInstalledUseCase
import com.kape.permissions.utils.GRANTED
import com.kape.permissions.utils.IDLE
import com.kape.permissions.utils.NOT_GRANTED
import com.kape.permissions.utils.PermissionsStep
import com.kape.permissions.utils.REQUEST
import com.kape.permissions.utils.VpnProfileState
import com.kape.router.EnterFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

class PermissionsViewModel(
    private val useCaseIsVpnProfileInstalled: IsVpnProfileInstalledUseCase,
    private val notificationPermissionManager: NotificationPermissionManager,
    private val router: Router,
) : ViewModel(), KoinComponent {

    private val _state = MutableStateFlow<PermissionsStep>(PermissionsStep.Vpn)
    val state: StateFlow<PermissionsStep> = _state

    private val _vpnPermissionState = MutableStateFlow(IDLE)
    val vpnPermissionState: StateFlow<VpnProfileState> = _vpnPermissionState

    fun checkFlowCompleted() = viewModelScope.launch {
        if (useCaseIsVpnProfileInstalled.isVpnProfileInstalled()) {
            if (isNotificationPermissionGranted()) {
                router.handleFlow(ExitFlow.Permissions)
            } else {
                _state.value = PermissionsStep.Notifications
            }
        }
    }

    fun onOkButtonClicked() = viewModelScope.launch {
        _vpnPermissionState.emit(REQUEST)
    }

    fun onVpnProfileStateChange() =
        viewModelScope.launch {
            if (useCaseIsVpnProfileInstalled.isVpnProfileInstalled()) {
                _vpnPermissionState.emit(GRANTED)
                _state.value = PermissionsStep.Notifications
                return@launch
            }
            _vpnPermissionState.emit(NOT_GRANTED)
        }

    fun isNotificationPermissionGranted(): Boolean =
        notificationPermissionManager.isNotificationsPermissionGranted()

    fun exitOnboarding() = router.handleFlow(ExitFlow.Permissions)
}