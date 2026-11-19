package com.kape.login.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.account.domain.LoginResult
import com.kape.account.domain.usecase.GetPostLoginDestinationUseCase
import com.kape.account.domain.usecase.LoginWithPurchaseHistoryUseCase
import com.kape.account.domain.usecase.ReceiptLoginProgress
import com.kape.contracts.AppInfo
import com.kape.contracts.NetworkStatus
import com.kape.contracts.Router
import com.kape.data.LoginWithEmail
import com.kape.login.domain.LoginFailureTracker
import com.kape.login.domain.usecase.BuildLoginSupportTicketUrlUseCase
import com.kape.login.domain.usecase.IsLoginSupportEnabledUseCase
import com.kape.login.domain.usecase.LoginWithCredentialsUseCase
import com.kape.login.ui.IDLE
import com.kape.login.ui.LOADING
import com.kape.login.ui.LoginScreenState
import com.kape.login.ui.toScreenState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

private const val GOOGLE_PLAY_FLAVOR = "google"

@KoinViewModel
class LoginViewModel(
    private val router: Router,
    private val loginWithCredentials: LoginWithCredentialsUseCase,
    private val loginWithPurchaseHistory: LoginWithPurchaseHistoryUseCase,
    private val getPostLoginDestination: GetPostLoginDestinationUseCase,
    private val failureTracker: LoginFailureTracker,
    private val isLoginSupportEnabled: IsLoginSupportEnabledUseCase,
    private val buildSupportTicketUrl: BuildLoginSupportTicketUrlUseCase,
    appInfo: AppInfo,
    networkStatus: NetworkStatus,
) : ViewModel() {
    private val _state = MutableStateFlow(IDLE)
    val state: StateFlow<LoginScreenState> = _state.asStateFlow()
    val isConnected: StateFlow<Boolean> = networkStatus.isOnline

    val shouldShowLoginWithReceiptButton: Boolean = appInfo.buildFlavor == GOOGLE_PLAY_FLAVOR

    private val _showSupportDialog = MutableStateFlow(false)
    val showSupportDialog: StateFlow<Boolean> = _showSupportDialog.asStateFlow()
    private var supportTicketUrl: String? = null

    private var receiptLoginJob: Job? = null

    fun login(
        username: String,
        password: String,
    ) = viewModelScope.launch {
        _state.value = LOADING
        val result = loginWithCredentials(username, password)
        if (result == LoginResult.Success) {
            failureTracker.recordSuccess()
            router.updateDestination(getPostLoginDestination())
            return@launch
        }
        _state.value = result.toScreenState()
        if (result is LoginResult.ServiceUnavailable && isLoginSupportEnabled()) {
            supportTicketUrl = buildSupportTicketUrl(result, username)
            if (failureTracker.recordServiceFailure()) {
                _showSupportDialog.value = true
            }
        } else {
            failureTracker.recordOtherFailure()
        }
    }

    fun getSupportTicketUrl(): String? = supportTicketUrl

    fun onSupportDialogDismissed() {
        _showSupportDialog.value = false
    }

    fun loginWithReceipt(packageName: String) {
        receiptLoginJob?.cancel()
        receiptLoginJob =
            viewModelScope.launch {
                loginWithPurchaseHistory(packageName).collect { progress ->
                    if (progress is ReceiptLoginProgress.Succeeded) {
                        router.updateDestination(progress.destination)
                    } else {
                        progress.toScreenState()?.let { _state.value = it }
                    }
                }
            }
    }

    fun navigateToLoginWithEmail() = router.updateDestination(LoginWithEmail)
}