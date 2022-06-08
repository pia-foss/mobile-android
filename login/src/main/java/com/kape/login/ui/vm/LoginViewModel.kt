package com.kape.login.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.login.domain.LoginUseCase
import com.kape.login.utils.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(private val useCase: LoginUseCase) : ViewModel() {

    private val _state = MutableStateFlow(IDLE)
    val loginState: StateFlow<LoginScreenState> = _state

    fun login(username: String, password: String) = viewModelScope.launch {
        _state.emit(LOADING)
        if (username.isEmpty() || password.isEmpty()) {
            _state.emit(INVALID)
            return@launch
        }
        useCase.login(username, password).collect {
            _state.emit(getScreenState(it))
        }
    }
}