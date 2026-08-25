package com.kape.welcomeback.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.contracts.Router
import com.kape.data.DI
import com.kape.data.LoginWithCredentials
import com.kape.data.TvLoginUsername
import com.kape.login.domain.mobile.LoginUseCase
import com.kape.login.domain.mobile.LoginWithReceiptHandler
import com.kape.login.utils.IDLE
import com.kape.login.utils.LOADING
import com.kape.login.utils.LoginScreenState
import com.kape.login.utils.LoginState
import com.kape.login.utils.RECEIPT_FAILED
import com.kape.login.utils.getReceiptScreenState
import com.kape.payments.utils.PurchaseHistoryState
import com.kape.permissions.utils.PermissionUtil
import com.kape.shareevents.data.KpiEventGenerator
import com.kape.shareevents.domain.SubmitKpiEventUseCase
import com.kape.utils.PlatformUtils
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Named

@KoinViewModel
class WelcomeBackViewModel(
    private val router: Router,
    private val loginUseCase: LoginUseCase,
    private val loginWithReceiptHandler: LoginWithReceiptHandler,
    private val permissionsUtil: PermissionUtil,
    private val submitKpiEventUseCase: SubmitKpiEventUseCase,
    private val eventGenerator: KpiEventGenerator,
    private val platformUtils: PlatformUtils,
    @Named(DI.IO_DISPATCHER) private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val _state = MutableStateFlow(IDLE)
    val state: StateFlow<LoginScreenState> = _state

    private var purchaseHistoryCollectorJob: Job? = null
    private lateinit var packageName: String

    fun onUsernameAndPasswordClicked() {
        if (platformUtils.isTv()) {
            router.updateDestination(TvLoginUsername)
        } else {
            router.updateDestination(LoginWithCredentials)
        }
    }

    // Same purchase-receipt login flow as LoginViewModel.loginWithReceipt(): look up the
    // Play Store purchase history, then log in with the resulting receipt token.
    fun onPlayStoreAccountClicked(packageName: String) {
        this.packageName = packageName
        viewModelScope.launch(ioDispatcher) {
            collectPurchaseHistory()
            loginWithReceiptHandler.getPurchaseHistory()
        }
    }

    private fun collectPurchaseHistory() {
        purchaseHistoryCollectorJob?.cancel()
        purchaseHistoryCollectorJob =
            viewModelScope.launch(ioDispatcher) {
                loginWithReceiptHandler.purchaseHistoryState.collectLatest {
                    _state.emit(LOADING)
                    when (it) {
                        is PurchaseHistoryState.PurchaseHistorySuccess -> {
                            val result =
                                loginUseCase.loginWithReceipt(
                                    it.purchaseToken,
                                    it.productId,
                                    packageName,
                                )
                            if (result == LoginState.Successful) {
                                submitKpiEventUseCase.submitEvent(eventGenerator.getProcessingSuccess())
                                router.updateDestination(permissionsUtil.getNextDestination())
                            } else {
                                _state.emit(getReceiptScreenState(result))
                            }
                        }

                        PurchaseHistoryState.Default -> _state.emit(IDLE)
                        PurchaseHistoryState.PurchaseHistoryFailed -> _state.emit(RECEIPT_FAILED)
                    }
                }
            }
    }
}