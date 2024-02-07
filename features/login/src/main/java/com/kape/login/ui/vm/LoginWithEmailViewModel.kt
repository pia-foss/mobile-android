package com.kape.login.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.login.domain.LoginUseCase
import com.kape.login.utils.IDLE
import com.kape.login.utils.INVALID
import com.kape.login.utils.LOADING
import com.kape.login.utils.LoginScreenState
import com.kape.login.utils.SUCCESS
import com.kape.utils.NetworkConnectionListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginWithEmailViewModel(
    private val useCase: LoginUseCase,
    networkConnectionListener: NetworkConnectionListener,
) : ViewModel() {

    private val _state = MutableStateFlow(IDLE)
    val loginState: StateFlow<LoginScreenState> = _state
    val isConnected = networkConnectionListener.isConnected

    fun loginWithEmail(email: String) = viewModelScope.launch {
        _state.emit(LOADING)
        if (email.isEmpty()) {
            _state.emit(INVALID)
            return@launch
        }
        useCase.loginWithEmail(email).collect {
            _state.emit(SUCCESS)
        }
    }
}