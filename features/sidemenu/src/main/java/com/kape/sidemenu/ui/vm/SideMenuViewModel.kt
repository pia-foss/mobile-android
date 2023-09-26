package com.kape.sidemenu.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.login.domain.LogoutUseCase
import com.kape.profile.domain.GetProfileUseCase
import com.kape.router.EnterFlow
import com.kape.router.ExitFlow
import com.kape.router.Router
import com.kape.sidemenu.utils.IDLE
import com.kape.sidemenu.utils.SideMenuState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

class SideMenuViewModel(
    private val profileUseCase: GetProfileUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val versionCode: Int,
    private val versionName: String,
    private val router: Router,
) :
    ViewModel(), KoinComponent {

    private val _state = MutableStateFlow(IDLE)
    val sideMenuState: StateFlow<SideMenuState> = _state

    init {
        viewModelScope.launch {
            profileUseCase.getProfile().collect {
                it?.let {
                    _state.emit(
                        SideMenuState(
                            it.username,
                            versionCode,
                            versionName,
                            it.subscription.showExpire,
                            it.subscription.daysRemaining,
                        ),
                    )
                }
            }
        }
    }

    fun logout() = viewModelScope.launch {
        logoutUseCase.logout().collect {
            router.handleFlow(EnterFlow.Subscribe)
        }
    }

    fun navigateToRegionSelection() {
        router.handleFlow(EnterFlow.RegionSelection)
    }

    fun navigateToProfile() {
        router.handleFlow(EnterFlow.Profile)
    }

    fun navigateToSettings() {
        router.handleFlow(EnterFlow.Settings)
    }

    fun navigateToPerAppSettings() {
        router.handleFlow(EnterFlow.PerAppSettings)
    }

    fun navigateToDedicatedIp() {
        router.handleFlow(EnterFlow.DedicatedIp)
    }

    fun exitDedicatedIp() {
        router.handleFlow(ExitFlow.DedicatedIp)
    }
}