package com.kape.login.ui.vm.tv

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class LoginPasswordViewModel(
    @InjectedParam private val username: String,
) : ViewModel() {
    private val password: MutableState<String> = mutableStateOf("")

    fun getLoginUsername(): String = username

    fun getPassword(): MutableState<String> = password

    fun isValidPassword(): Boolean = password.value.isNotEmpty()
}