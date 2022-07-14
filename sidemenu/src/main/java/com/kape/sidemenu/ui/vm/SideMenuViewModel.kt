package com.kape.sidemenu.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.profile.domain.GetProfileUseCase
import com.kape.sidemenu.utils.IDLE
import com.kape.sidemenu.utils.SideMenuState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SideMenuViewModel(private val profileUseCase: GetProfileUseCase, private val versionCode: Int, private val versionName: String) :
    ViewModel() {

    private val _state = MutableStateFlow(IDLE)
    val sideMenuState: StateFlow<SideMenuState> = _state

    init {
        viewModelScope.launch {
            profileUseCase.getProfile().collect {
                it?.let {
                    _state.emit(SideMenuState(it.username,
                        versionCode,
                        versionName,
                        it.subscription.showExpire,
                        it.subscription.daysRemaining))
                }
            }
        }
    }
}
