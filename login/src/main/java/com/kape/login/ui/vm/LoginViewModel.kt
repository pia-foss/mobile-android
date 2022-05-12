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
        _state.emit(LOADING)
        if (username.isEmpty() || password.isEmpty()) {
            _state.emit(INVALID)
            return@launch
        }
        useCase.login(username, password).collect {
            _state.emit(getScreenState(it))
        }
    }

    data class LoginScreenState(val idle: Boolean, val loading: Boolean, val error: LoginError?, val flowCompleted: Boolean)

    companion object {
        val IDLE = LoginScreenState(idle = true, loading = false, error = null, flowCompleted = false)
        val LOADING = LoginScreenState(idle = false, loading = true, error = null, flowCompleted = false)
        val SUCCESS = LoginScreenState(idle = true, loading = false, error = null, flowCompleted = true)
        val THROTTLED = LoginScreenState(idle = true, loading = false, error = LoginError.Throttled, flowCompleted = false)
        val FAILED = LoginScreenState(idle = true, loading = false, error = LoginError.Failed, flowCompleted = false)
        val EXPIRED = LoginScreenState(idle = true, loading = false, error = LoginError.Expired, flowCompleted = false)
        val INVALID = LoginScreenState(idle = true, loading = false, error = LoginError.Invalid, flowCompleted = false)
        val SERVICE_UNAVAILABLE = LoginScreenState(idle = true, loading = false, error = LoginError.ServiceUnavailable, flowCompleted = false)
    }

    private fun getScreenState(state: LoginState): LoginScreenState = when (state) {
        LoginState.Expired -> EXPIRED
        LoginState.Failed -> FAILED
        LoginState.Successful -> SUCCESS
        LoginState.Throttled -> THROTTLED
        LoginState.ServiceUnavailable -> SERVICE_UNAVAILABLE
    }

    sealed class LoginError {
        object Invalid : LoginError()
        object Throttled : LoginError()
        object Failed : LoginError()
        object Expired : LoginError()
        object ServiceUnavailable: LoginError()
    }
}