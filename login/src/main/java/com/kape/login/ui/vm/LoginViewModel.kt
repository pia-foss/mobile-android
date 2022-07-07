package com.kape.login.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.login.domain.GetUserLoggedInUseCase
import com.kape.login.domain.LoginUseCase
import com.kape.login.utils.*
import com.kape.router.ExitFlow
import com.kape.router.Router
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class LoginViewModel(private val loginUseCase: LoginUseCase, private val userLoggedInUseCase: GetUserLoggedInUseCase) : ViewModel(),
    KoinComponent {

    private val router: Router by inject()
    private val _state = MutableStateFlow(IDLE)
    val loginState: StateFlow<LoginScreenState> = _state

    fun checkUserLoggedIn() {
        if (userLoggedInUseCase.isUserLoggedIn()) {
            router.handleFlow(ExitFlow.Login)
        }
    }

    fun login(username: String, password: String) = viewModelScope.launch {
        _state.emit(LOADING)
        if (username.isEmpty() || password.isEmpty()) {
            _state.emit(INVALID)
            return@launch
        }
        loginUseCase.login(username, password).collect {
            if (it == LoginState.Successful) {
                router.handleFlow(ExitFlow.Login)
                return@collect
            }
            _state.emit(getScreenState(it))
        }
    }
}