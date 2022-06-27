package com.kape.profile.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.profile.R
import com.kape.profile.domain.GetProfileUseCase
import com.kape.profile.models.Profile
import com.kape.profile.ui.*
import com.kape.uicomponents.components.UiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(private val useCase: GetProfileUseCase) : ViewModel() {
    private val _state = MutableStateFlow(IDLE)
    val screenState: StateFlow<ProfileScreenState> = _state

    init {
        loadProfile()
    }

    fun loadProfile() = viewModelScope.launch {
        _state.emit(LOADING)
        useCase.getProfile().collect { profile ->
            _state.emit(getState(profile))
        }
    }

    private fun getState(profile: Profile?): ProfileScreenState {
        return if (profile != null) {
            val expirationMessage: UiText
            val expirationDate: UiText
            if (profile.subscription.isExpired) {
                expirationMessage = UiText.StringResource(R.string.message_expiration)
                expirationDate = UiText.DynamicString(profile.subscription.expirationDate)
            } else {
                expirationMessage = UiText.StringResource(R.string.message_expired)
                expirationDate = UiText.StringResource(R.string.subscription_status_expired)
            }
            createSuccessState(expirationMessage, expirationDate)
        } else {
            NO_PROFILE
        }
    }
}