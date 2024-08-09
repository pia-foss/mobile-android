package com.kape.login.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.buildconfig.data.BuildConfigProvider
import com.kape.login.domain.mobile.GetUserLoggedInUseCase
import com.kape.login.domain.mobile.LoginUseCase
import com.kape.login.utils.FAILED
import com.kape.login.utils.IDLE
import com.kape.login.utils.INVALID
import com.kape.login.utils.LOADING
import com.kape.login.utils.LoginScreenState
import com.kape.login.utils.LoginState
import com.kape.login.utils.getScreenState
import com.kape.payments.ui.VpnSubscriptionPaymentProvider
import com.kape.payments.utils.PurchaseHistoryState
import com.kape.router.ExitFlow
import com.kape.router.Router
import com.kape.utils.NetworkConnectionListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    private val userLoggedInUseCase: GetUserLoggedInUseCase,
    private val vpnSubscriptionPaymentProvider: VpnSubscriptionPaymentProvider,
    private val router: Router,
    private val buildConfigProvider: BuildConfigProvider,
    networkConnectionListener: NetworkConnectionListener,
) : ViewModel(), KoinComponent {

    private val _state = MutableStateFlow(IDLE)
    val loginState: StateFlow<LoginScreenState> = _state
    val isConnected = networkConnectionListener.isConnected
    val shouldShowLoginWithReceiptButton: Boolean = buildConfigProvider.isGoogleFlavor()

    private lateinit var packageName: String

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

    fun loginWithReceipt(packageName: String) {
        this.packageName = packageName
        viewModelScope.launch {
            collectPurchaseHistory()
            vpnSubscriptionPaymentProvider.getPurchaseHistory()
        }
    }

    private fun collectPurchaseHistory() {
        viewModelScope.launch {
            vpnSubscriptionPaymentProvider.purchaseHistoryState.collect {
                _state.emit(LOADING)
                when (it) {
                    is PurchaseHistoryState.PurchaseHistorySuccess -> loginUseCase.loginWithReceipt(
                        it.purchaseToken,
                        it.productId,
                        packageName,
                    ).collect { state ->
                        if (state == LoginState.Successful) {
                            router.handleFlow(ExitFlow.Login)
                            return@collect
                        }
                        _state.emit(getScreenState(state))
                    }

                    PurchaseHistoryState.Default -> {
                        _state.emit(IDLE)
                    }

                    PurchaseHistoryState.PurchaseHistoryFailed -> _state.emit(FAILED)
                }
            }
        }
    }
}