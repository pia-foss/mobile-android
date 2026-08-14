package com.kape.login.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.buildconfig.data.BuildConfigProvider
import com.kape.contracts.AppInfo
import com.kape.contracts.DeviceInfo
import com.kape.contracts.Router
import com.kape.data.DI
import com.kape.data.LoginWithEmail
import com.kape.localprefs.prefs.FeaturePrefs
import com.kape.localprefs.prefs.SUPPORT_DIALOG_FEATURE_FLAG
import com.kape.login.domain.mobile.LoginFailureTracker
import com.kape.login.domain.mobile.LoginUseCase
import com.kape.login.domain.mobile.LoginWithReceiptHandler
import com.kape.login.utils.IDLE
import com.kape.login.utils.INVALID
import com.kape.login.utils.LOADING
import com.kape.login.utils.LoginScreenState
import com.kape.login.utils.LoginState
import com.kape.login.utils.QualifyingFailure
import com.kape.login.utils.RECEIPT_FAILED
import com.kape.login.utils.SupportTicketInfo
import com.kape.login.utils.buildSupportTicketUrl
import com.kape.login.utils.getReceiptScreenState
import com.kape.login.utils.getScreenState
import com.kape.login.utils.maskAccountIdentifier
import com.kape.payments.utils.PurchaseHistoryState
import com.kape.permissions.utils.PermissionUtil
import com.kape.shareevents.data.KpiEventGenerator
import com.kape.shareevents.domain.SubmitKpiEventUseCase
import com.kape.utils.NetworkConnectionListener
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Named
import kotlin.time.Clock

@KoinViewModel
class LoginViewModel(
    private val router: Router,
    private val loginUseCase: LoginUseCase,
    private val loginWithReceiptHandler: LoginWithReceiptHandler,
    private val buildConfigProvider: BuildConfigProvider,
    private val permissionsUtil: PermissionUtil,
    private val submitKpiEventUseCase: SubmitKpiEventUseCase,
    private val appInfo: AppInfo,
    private val deviceInfo: DeviceInfo,
    private val loginFailureTracker: LoginFailureTracker,
    private val featurePrefs: FeaturePrefs,
    private val eventGenerator: KpiEventGenerator,
    @Named(DI.IO_DISPATCHER) private val ioDispatcher: CoroutineDispatcher,
    networkConnectionListener: NetworkConnectionListener,
) : ViewModel() {
    private val _state = MutableStateFlow(IDLE)
    val state: StateFlow<LoginScreenState> = _state
    val isConnected = networkConnectionListener.isConnected
    val shouldShowLoginWithReceiptButton: Boolean = buildConfigProvider.isGoogleFlavor()

    private val _showSupportDialog = MutableStateFlow(false)
    val showSupportDialog: StateFlow<Boolean> = _showSupportDialog

    private var pendingSupportTicket: SupportTicketInfo? = null

    private lateinit var packageName: String

    fun login(
        username: String,
        password: String,
    ) = viewModelScope.launch(ioDispatcher) {
        _state.emit(LOADING)
        if (username.isEmpty() || password.isEmpty()) {
            _state.emit(INVALID)
            return@launch
        }
        val it = loginUseCase.login(username, password)
        if (it == LoginState.Successful) {
            loginFailureTracker.recordSuccess()
            router.updateDestination(permissionsUtil.getNextDestination())
            return@launch
        }
        _state.emit(getScreenState(it))
        if (it is QualifyingFailure && isSupportDialogEnabled()) {
            pendingSupportTicket = buildSupportTicketInfo(it, username)
            if (loginFailureTracker.recordQualifyingFailure()) {
                _showSupportDialog.emit(true)
            }
        } else {
            loginFailureTracker.recordNonQualifyingFailure()
        }
    }

    private suspend fun isSupportDialogEnabled(): Boolean = featurePrefs.getFlags().first().contains(SUPPORT_DIALOG_FEATURE_FLAG)

    fun onSupportDialogDismissed() =
        viewModelScope.launch {
            _showSupportDialog.emit(false)
        }

    fun getSupportTicketUrl(): String? = pendingSupportTicket?.let(::buildSupportTicketUrl)

    private fun buildSupportTicketInfo(
        failure: QualifyingFailure,
        username: String,
    ) = SupportTicketInfo(
        errorCode = failure.code,
        errorMessage = failure.message,
        timestampUtc = Clock.System.now().toString(),
        appVersionName = appInfo.versionName,
        appVersionCode = appInfo.versionCode,
        osVersion = deviceInfo.osVersion,
        manufacturer = deviceInfo.manufacturer,
        model = deviceInfo.model,
        maskedAccountId = maskAccountIdentifier(username),
    )

    fun loginWithReceipt(packageName: String) {
        this.packageName = packageName
        viewModelScope.launch(ioDispatcher) {
            collectPurchaseHistory()
            loginWithReceiptHandler.getPurchaseHistory()
        }
    }

    fun navigateToLoginWithEmail() = router.updateDestination(LoginWithEmail)

    private fun collectPurchaseHistory() {
        viewModelScope.launch(ioDispatcher) {
            loginWithReceiptHandler.purchaseHistoryState.collect {
                _state.emit(LOADING)
                when (it) {
                    is PurchaseHistoryState.PurchaseHistorySuccess -> {
                        val state =
                            loginUseCase.loginWithReceipt(
                                it.purchaseToken,
                                it.productId,
                                packageName,
                            )
                        if (state == LoginState.Successful) {
                            submitKpiEventUseCase.submitEvent(eventGenerator.getProcessingSuccess())
                            router.updateDestination(permissionsUtil.getNextDestination())
                        } else {
                            _state.emit(getReceiptScreenState(state))
                        }
                    }

                    PurchaseHistoryState.Default -> {
                        _state.emit(IDLE)
                    }

                    PurchaseHistoryState.PurchaseHistoryFailed -> _state.emit(RECEIPT_FAILED)
                }
            }
        }
    }
}