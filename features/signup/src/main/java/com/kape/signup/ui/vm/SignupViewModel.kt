package com.kape.signup.ui.vm

import android.app.Activity
import android.util.Patterns
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.account.domain.usecase.GetPostLoginDestinationUseCase
import com.kape.contracts.NetworkStatus
import com.kape.contracts.Router
import com.kape.data.LoginWithCredentials
import com.kape.data.TvLoginUsername
import com.kape.data.TvWelcome
import com.kape.data.WebDestination
import com.kape.signup.domain.StoreOffer
import com.kape.signup.domain.SubscriptionStore
import com.kape.signup.domain.usecase.CompleteSignupUseCase
import com.kape.signup.domain.usecase.GetPostPurchaseStepUseCase
import com.kape.signup.domain.usecase.PostPurchaseStep
import com.kape.signup.domain.usecase.SignupResult
import com.kape.signup.domain.usecase.SubmitConsentUseCase
import com.kape.signup.ui.AMAZON_LOGIN
import com.kape.signup.ui.CONSENT
import com.kape.signup.ui.DEFAULT
import com.kape.signup.ui.EMAIL
import com.kape.signup.ui.ERROR_EMAIL_INVALID
import com.kape.signup.ui.ERROR_REGISTRATION
import com.kape.signup.ui.IN_PROCESS
import com.kape.signup.ui.LOADING
import com.kape.signup.ui.NO_IN_APP_SUBSCRIPTIONS
import com.kape.signup.ui.SUBSCRIPTIONS_FAILED_TO_LOAD
import com.kape.signup.ui.SignupScreenState
import com.kape.signup.ui.SubscriptionData
import com.kape.signup.ui.signedUp
import com.kape.signup.ui.subscriptions
import com.kape.utils.PlatformUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class SignupViewModel(
    private val router: Router,
    private val store: SubscriptionStore,
    private val submitConsent: SubmitConsentUseCase,
    private val getPostPurchaseStep: GetPostPurchaseStepUseCase,
    private val completeSignup: CompleteSignupUseCase,
    private val getPostLoginDestination: GetPostLoginDestinationUseCase,
    private val platformUtils: PlatformUtils,
    networkStatus: NetworkStatus,
) : ViewModel() {
    private val _state = MutableStateFlow(DEFAULT)
    val state: StateFlow<SignupScreenState> = _state.asStateFlow()
    val isConnected: StateFlow<Boolean> = networkStatus.isOnline

    init {
        viewModelScope.launch {
            store.offer.collect { _state.value = it.toScreenState() }
        }
        viewModelScope.launch {
            store.purchaseCompleted.collect {
                _state.value =
                    when (getPostPurchaseStep()) {
                        PostPurchaseStep.Consent -> CONSENT
                        PostPurchaseStep.Email -> EMAIL
                    }
            }
        }
    }

    fun loadPrices(activity: Activity) =
        viewModelScope.launch {
            if (store.hasResumablePurchase()) {
                _state.value = EMAIL
            } else {
                store.loadPlans(activity)
            }
        }

    fun loadEmptyPrices() {
        _state.value = SUBSCRIPTIONS_FAILED_TO_LOAD
    }

    fun purchase(
        id: String,
        activity: Activity,
    ) = store.purchase(id, activity)

    fun registerClientIfNeeded(activity: Activity) = store.register(activity)

    fun navigateToLogin() {
        router.updateDestination(LoginWithCredentials)
        store.reset()
    }

    fun navigateToTvLogin() {
        router.updateDestination(TvLoginUsername)
        store.reset()
    }

    fun navigateToTvWelcome() = router.updateDestination(TvWelcome)

    fun navigateToPrivacyPolicy() = router.updateDestination(WebDestination.Privacy)

    fun navigateToTermsOfService() = router.updateDestination(WebDestination.Terms)

    fun navigateToWebsite() = router.updateDestination(WebDestination.NoInAppRegistration)

    fun allowEventSharing(
        allow: Boolean,
        isFirstScreen: Boolean,
    ) = viewModelScope.launch {
        val destination = submitConsent(allow, isFirstScreen, platformUtils.isTv())
        if (destination != null) {
            router.updateDestination(destination)
        } else {
            _state.value = EMAIL
        }
    }

    fun isValidEmail(email: String): Boolean = Patterns.EMAIL_ADDRESS.matcher(email).matches()

    fun register(email: String) =
        viewModelScope.launch {
            _state.value = IN_PROCESS
            _state.value =
                when (val result = completeSignup(email)) {
                    SignupResult.InvalidEmail -> ERROR_EMAIL_INVALID
                    SignupResult.Failed -> ERROR_REGISTRATION
                    is SignupResult.SignedUp -> signedUp(result.credentials)
                }
        }

    fun completeSubscription() {
        router.updateDestination(getPostLoginDestination())
        store.reset()
    }

    // Fully qualified: the domain function has the same name as this one.
    fun isoDurationToDays(duration: String?): Int? =
        com.kape.signup.domain
            .isoDurationToDays(duration)

    private fun StoreOffer.toScreenState(): SignupScreenState =
        when (this) {
            StoreOffer.None -> DEFAULT
            StoreOffer.Loading -> LOADING
            is StoreOffer.Plans -> subscriptions(SubscriptionData(mutableStateOf(yearly), yearly, monthly))
            StoreOffer.PlansUnavailable -> SUBSCRIPTIONS_FAILED_TO_LOAD
            StoreOffer.WebsiteOnly -> NO_IN_APP_SUBSCRIPTIONS
            StoreOffer.StoreLoginOnly -> AMAZON_LOGIN
        }
}