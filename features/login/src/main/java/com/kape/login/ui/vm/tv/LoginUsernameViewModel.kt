package com.kape.login.ui.vm.tv

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.kape.contracts.Router
import com.kape.data.TvLoginPassword
import com.kape.login.domain.tv.LoginUsernameUseCase
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class LoginUsernameViewModel(
    private val router: Router,
    private val loginUsernameUseCase: LoginUsernameUseCase,
) : ViewModel(){

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