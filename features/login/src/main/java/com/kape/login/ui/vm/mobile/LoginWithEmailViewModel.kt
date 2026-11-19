package com.kape.login.ui.vm.mobile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.contracts.NetworkStatus
import com.kape.contracts.Router
import com.kape.data.LoginWithCredentials
import com.kape.login.domain.usecase.RequestLoginLinkUseCase
import com.kape.login.ui.IDLE
import com.kape.login.ui.LOADING
import com.kape.login.ui.LoginScreenState
import com.kape.login.ui.toScreenState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class LoginWithEmailViewModel(
    private val router: Router,
    private val requestLoginLink: RequestLoginLinkUseCase,
    networkStatus: NetworkStatus,
) : ViewModel() {
    private val _state = MutableStateFlow(IDLE)
    val state: StateFlow<LoginScreenState> = _state.asStateFlow()
    val isConnected: StateFlow<Boolean> = networkStatus.isOnline

    fun loginWithEmail(email: String) =
        viewModelScope.launch {
            _state.value = LOADING
            _state.value = requestLoginLink(email).toScreenState()
        }

    fun navigateToLoginWithCredentials() = router.updateDestination(LoginWithCredentials)
}