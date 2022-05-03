package com.kape.login.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.login.domain.LoginUseCase
import com.kape.login.utils.LoginState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(private val useCase: LoginUseCase) : ViewModel() {

    private val _state = MutableStateFlow(IDLE)
    val loginState: StateFlow<LoginScreenState> = _state

    fun login(username: String, password: String) = viewModelScope.launch {
        _state.value = LOADING
        useCase.login(username, password).collect {
            when (it) {
                LoginState.Expired -> _state.value = EXPIRED
                LoginState.Failed -> _state.value = FAILED
                LoginState.Successful -> _state.value = SUCCESS
                LoginState.Throttled -> _state.value = THROTTLED
            }
        }
    }

    data class LoginScreenState(val idle: Boolean, val loading: Boolean, val error: String?, val flowCompleted: Boolean)

    companion object {
        val IDLE = LoginScreenState(idle = true, loading = false, error = null, flowCompleted = false)
        val LOADING = LoginScreenState(idle = false, loading = true, error = null, flowCompleted = false)
        val SUCCESS = LoginScreenState(idle = true, loading = false, error = null, flowCompleted = true)
        val THROTTLED = LoginScreenState(idle = true, loading = false, error = "throttled", flowCompleted = false)
        val FAILED = LoginScreenState(idle = true, loading = false, error = "failed auth", flowCompleted = false)
        val EXPIRED = LoginScreenState(idle = true, loading = false, error = "expired account", flowCompleted = false)
    }
}