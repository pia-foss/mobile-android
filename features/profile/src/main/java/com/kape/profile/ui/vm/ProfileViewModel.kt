package com.kape.profile.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.profile.domain.GetProfileUseCase
import com.kape.profile.data.models.Profile
import com.kape.profile.ui.IDLE
import com.kape.profile.ui.LOADING
import com.kape.profile.ui.ProfileScreenState
import com.kape.profile.ui.createSuccessState
import com.kape.router.Back
import com.kape.router.Router
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ProfileViewModel(private val useCase: GetProfileUseCase) : ViewModel(), KoinComponent {

    private val router: Router by inject()
    private val _state = MutableStateFlow(IDLE)
    val screenState: StateFlow<ProfileScreenState> = _state

    init {
        loadProfile()
    }

    fun navigateBack() {
        router.handleFlow(Back)
    }

    private fun loadProfile() = viewModelScope.launch {
        _state.emit(LOADING)
        useCase.getProfile().collect { profile ->
            if (profile == null) {
                _state.emit(IDLE)
            } else {
                _state.emit(getState(profile))
            }
        }
    }

    private fun getState(profile: Profile): ProfileScreenState {
        return createSuccessState(profile.username, profile.subscription.expirationDate, profile.subscription.isExpired)
    }
}