package com.kape.profile.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.profile.domain.GetProfileUseCase
import com.kape.profile.ui.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ProfileViewModel(private val useCase: GetProfileUseCase) : ViewModel() {

    private val _state = MutableStateFlow(IDLE)
    val screenState: StateFlow<ProfileScreenState> = _state

    fun loadProfile() = viewModelScope.launch {
        _state.emit(LOADING)
        useCase.getProfile().collect { profile ->
            if (profile != null) {
                _state.emit(SUCCESS)
            } else {
                _state.emit(ERROR_NO_PROFILE)
            }
        }
    }
}