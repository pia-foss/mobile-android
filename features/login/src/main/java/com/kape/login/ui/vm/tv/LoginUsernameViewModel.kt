package com.kape.login.ui.vm.tv

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.kape.login.domain.tv.LoginUsernameUseCase
import com.kape.router.Router
import com.kape.router.TvLoginPassword
import org.koin.core.component.KoinComponent

class LoginUsernameViewModel(
    private val router: Router,
    private val loginUsernameUseCase: LoginUsernameUseCase,
) : ViewModel(), KoinComponent {

    private var username: MutableState<String> = mutableStateOf("")

    fun setLoginUsername(loginUsername: String) {
        loginUsernameUseCase.setLoginUsername(loginUsername = loginUsername)
    }

    fun navigateToPassword() {
        router.updateDestination(TvLoginPassword)
    }

    fun isValidUsername(): Boolean =
        username.value.isNotEmpty()

    fun getUsername(): MutableState<String> =
        username
}