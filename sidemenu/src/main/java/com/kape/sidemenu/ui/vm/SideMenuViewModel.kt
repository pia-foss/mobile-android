package com.kape.sidemenu.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.login.domain.LogoutUseCase
import com.kape.profile.domain.GetProfileUseCase
import com.kape.router.EnterFlow
import com.kape.router.Router
import com.kape.sidemenu.utils.IDLE
import com.kape.sidemenu.utils.SideMenuState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SideMenuViewModel(
    private val profileUseCase: GetProfileUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val versionCode: Int,
    private val versionName: String
) :
    ViewModel(), KoinComponent {

    private val router: Router by inject()
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
                            it.subscription.daysRemaining
                        )
                    )
                }
            }
        }
    }

    fun logout() = viewModelScope.launch {
        logoutUseCase.logout().collect {
            // TODO: go to signup screen once implemented
            router.handleFlow(EnterFlow.Login)
        }
    }

    fun navigateToRegionSelection() {
        router.handleFlow(EnterFlow.RegionSelection)
    }
}
