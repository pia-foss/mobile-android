package com.kape.login.ui.vm.tv

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.kape.contracts.Router
import com.kape.data.TvLoginPassword
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class LoginUsernameViewModel(
    private val router: Router,
) : ViewModel() {
    private val username: MutableState<String> = mutableStateOf("")

    fun getUsername(): MutableState<String> = username

    fun isValidUsername(): Boolean = username.value.isNotEmpty()

    fun navigateToPassword() = router.updateDestination(TvLoginPassword(username.value))
}