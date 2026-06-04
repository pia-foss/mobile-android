package com.kape.signup.ui.vm

import android.app.Activity
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.contracts.Router
import com.kape.data.DI
import com.kape.data.LoginWithCredentials
import com.kape.data.TvWelcome
import com.kape.data.WebDestination
import com.kape.permissions.utils.PermissionUtil
import com.kape.signup.domain.ConsentUseCase
import com.kape.signup.domain.SignupBillingHandler
import com.kape.signup.domain.SignupUseCase
import com.kape.signup.utils.DEFAULT
import com.kape.signup.utils.EMAIL
import com.kape.signup.utils.ERROR_EMAIL_INVALID
import com.kape.signup.utils.ERROR_REGISTRATION
import com.kape.signup.utils.IN_PROCESS
import com.kape.signup.utils.SUBSCRIPTIONS_FAILED_TO_LOAD
import com.kape.signup.utils.SignupScreenState
import com.kape.signup.utils.signedUp
import com.kape.utils.NetworkConnectionListener
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Named

@KoinViewModel
class SignupViewModel(
    private val router: Router,
    private val billingHandler: SignupBillingHandler,
    private val consentUseCase: ConsentUseCase,
    private val useCase: SignupUseCase,
    private val permissionUtil: PermissionUtil,
    @Named(DI.IO_DISPATCHER) private val ioDispatcher: CoroutineDispatcher,
    networkConnectionListener: NetworkConnectionListener,
) : ViewModel() {
    private val _state = MutableStateFlow(DEFAULT)
    val state: StateFlow<SignupScreenState> = _state

    val isConnected = networkConnectionListener.isConnected

    init {
        viewModelScope.launch(ioDispatcher) {
            billingHandler.billingState.collect { _state.emit(it) }
        }
        billingHandler.initialize(viewModelScope, ioDispatcher)
    }

    fun loadPrices() = billingHandler.loadPrices(viewModelScope, ioDispatcher)

    fun loadEmptyPrices() =
        viewModelScope.launch(ioDispatcher) {
            _state.emit(SUBSCRIPTIONS_FAILED_TO_LOAD)
        }

    fun purchase(id: String) =
        viewModelScope.launch(ioDispatcher) {
            billingHandler.purchase(id)
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

    fun allowEventSharing(allow: Boolean) =
        viewModelScope.launch(ioDispatcher) {
            consentUseCase.setConsent(allow)
            _state.emit(EMAIL)
        }

    fun isValidEmail(email: String): Boolean = Patterns.EMAIL_ADDRESS.matcher(email).matches()

    fun register(email: String) =
        viewModelScope.launch(ioDispatcher) {
            if (email.isEmpty()) {
                _state.emit(ERROR_EMAIL_INVALID)
                return@launch
            }
            _state.emit(IN_PROCESS)
            val result = useCase.vpnSignup(email)
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
}