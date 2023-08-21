package com.kape.login.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.login.domain.GetUserLoggedInUseCase
import com.kape.login.domain.LoginUseCase
import com.kape.login.utils.FAILED
import com.kape.login.utils.IDLE
import com.kape.login.utils.INVALID
import com.kape.login.utils.LOADING
import com.kape.login.utils.LoginScreenState
import com.kape.login.utils.LoginState
import com.kape.login.utils.getScreenState
import com.kape.payments.ui.PaymentProvider
import com.kape.payments.utils.PurchaseHistoryState
import com.kape.router.ExitFlow
import com.kape.router.Router
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    private val userLoggedInUseCase: GetUserLoggedInUseCase,
    private val paymentProvider: PaymentProvider,
    private val router: Router
) : ViewModel(), KoinComponent {

    private val _state = MutableStateFlow(IDLE)
    val loginState: StateFlow<LoginScreenState> = _state

    private lateinit var packageName: String

    init {
        viewModelScope.launch {
            paymentProvider.purchaseHistoryState.collect {
                _state.emit(LOADING)
                when (it) {
                    is PurchaseHistoryState.PurchaseHistorySuccess -> loginUseCase.loginWithReceipt(
                        it.purchaseToken,
                        it.productId,
                        packageName
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
            paymentProvider.getPurchaseHistory()
        }
    }
}