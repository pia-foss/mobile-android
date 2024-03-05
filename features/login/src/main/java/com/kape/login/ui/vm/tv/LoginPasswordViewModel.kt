package com.kape.login.ui.vm.tv

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.kape.login.domain.tv.LoginUsernameUseCase
import org.koin.core.component.KoinComponent

class LoginPasswordViewModel(
    private val loginUsernameUseCase: LoginUsernameUseCase,
) : ViewModel(), KoinComponent {

    private var password: MutableState<String> = mutableStateOf("")

    fun isValidPassword(): Boolean =
        password.value.isNotEmpty()

    fun getPassword(): MutableState<String> =
        password

    fun getLoginUsername(): String =
        loginUsernameUseCase.getLoginUsername()
}