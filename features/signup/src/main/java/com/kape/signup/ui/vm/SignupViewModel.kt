package com.kape.signup.ui.vm

import android.app.Activity
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.contracts.Router
import com.kape.data.DI
import com.kape.data.LoginWithCredentials
import com.kape.data.Subscribe
import com.kape.data.TvWelcome
import com.kape.data.TvWelcomeBack
import com.kape.data.WebDestination
import com.kape.data.WelcomeBack
import com.kape.permissions.utils.PermissionUtil
import com.kape.shareevents.data.KpiEventGenerator
import com.kape.shareevents.domain.SubmitKpiEventUseCase
import com.kape.signup.domain.ConsentUseCase
import com.kape.signup.domain.SignupBillingHandler
import com.kape.signup.domain.SignupHandler
import com.kape.signup.utils.DEFAULT
import com.kape.signup.utils.EMAIL
import com.kape.signup.utils.ERROR_EMAIL_INVALID
import com.kape.signup.utils.ERROR_REGISTRATION
import com.kape.signup.utils.IN_PROCESS
import com.kape.signup.utils.SUBSCRIPTIONS_FAILED_TO_LOAD
import com.kape.signup.utils.SignupScreenState
import com.kape.signup.utils.signedUp
import com.kape.utils.NetworkConnectionListener
import com.kape.utils.PlatformUtils
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Named

@KoinViewModel
class SignupViewModel(
    private val router: Router,
    private val billingHandler: SignupBillingHandler,
    private val consentUseCase: ConsentUseCase,
    private val signupHandler: SignupHandler,
    private val permissionUtil: PermissionUtil,
    private val submitKpiEventUseCase: SubmitKpiEventUseCase,
    private val platformUtils: PlatformUtils,
    private val eventGenerator: KpiEventGenerator,
    @Named(DI.IO_DISPATCHER) private val ioDispatcher: CoroutineDispatcher,
    @Named(DI.MAIN_DISPATCHER) private val mainDispatcher: CoroutineDispatcher,
    networkConnectionListener: NetworkConnectionListener,
) : ViewModel() {
    private val _state =
        MutableStateFlow(billingHandler.billingState.replayCache.firstOrNull() ?: DEFAULT)
    val state: StateFlow<SignupScreenState> = _state

    val isConnected = networkConnectionListener.isConnected

    init {
        viewModelScope.launch(ioDispatcher) {
            billingHandler.billingState.collect { _state.emit(it) }
        }
        billingHandler.initialize(mainDispatcher)
    }

    fun loadPrices(activity: Activity) =
        viewModelScope.launch(ioDispatcher) {
            if (billingHandler.hasResumablePurchase()) {
                _state.emit(EMAIL)
            } else {
                billingHandler.loadPrices(mainDispatcher, activity)
            }
        }

    fun loadEmptyPrices() =
        viewModelScope.launch(ioDispatcher) {
            _state.emit(SUBSCRIPTIONS_FAILED_TO_LOAD)
        }

    fun purchase(
        id: String,
        activity: Activity,
    ) = viewModelScope.launch(ioDispatcher) {
        submitKpiEventUseCase.submitEvent(eventGenerator.getProcessingPurchaseEvent())
        billingHandler.purchase(id, activity)
    }

    fun navigateToLogin() {
        router.updateDestination(LoginWithCredentials)
        billingHandler.reset()
    }

    fun navigateToTvWelcome() {
        router.updateDestination(TvWelcome)
    }

    fun navigateToPrivacyPolicy() {
        router.updateDestination(WebDestination.Privacy)
    }

    fun navigateToTermsOfService() {
        router.updateDestination(WebDestination.Terms)
    }

    fun navigateToWebsite() {
        router.updateDestination(WebDestination.NoInAppRegistration)
    }

    fun allowEventSharing(
        allow: Boolean,
        isFirstScreen: Boolean,
    ) = viewModelScope.launch(ioDispatcher) {
        consentUseCase.setConsent(allow)
        if (isFirstScreen) {
            if (platformUtils.isTv()) {
                if (billingHandler.hasActiveSubscription().first()) {
                    router.updateDestination(TvWelcomeBack)
                } else {
                    router.updateDestination(TvWelcome)
                }
            } else if (billingHandler.hasActiveSubscription().first()) {
                router.updateDestination(WelcomeBack)
            } else {
                router.updateDestination(Subscribe)
            }
        } else {
            _state.emit(EMAIL)
        }
    }

    fun isValidEmail(email: String): Boolean = Patterns.EMAIL_ADDRESS.matcher(email).matches()

    fun register(email: String) =
        viewModelScope.launch(ioDispatcher) {
            if (email.isEmpty()) {
                _state.emit(ERROR_EMAIL_INVALID)
                return@launch
            }
            _state.emit(IN_PROCESS)
            val result = signupHandler.vpnSignup(email)
            if (result == null) {
                _state.emit(ERROR_REGISTRATION)
            } else {
                _state.emit(signedUp(result))
            }
        }

    fun completeSubscription() {
        router.updateDestination(permissionUtil.getNextDestination())
        billingHandler.reset()
    }

    fun registerClientIfNeeded(activity: Activity) {
        billingHandler.registerClientIfNeeded(activity)
    }

    fun isoDurationToDays(duration: String?): Int? {
        val regex = Regex("^P(\\d+)([DWMY])$")
        return duration?.let {
            val match =
                regex.matchEntire(duration)
                    ?: throw IllegalArgumentException("Unsupported duration format: $duration")

            val (valueStr, unit) = match.destructured
            val value = valueStr.toInt()

            return when (unit) {
                "D" -> value * 1
                "W" -> value * 7
                "M" -> value * 30 // approximation
                "Y" -> value * 365 // approximation
                else -> null
            }
        }
    }
}