package com.kape.signup.ui.vm

import android.app.Activity
import android.util.Patterns
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.buildconfig.data.BuildConfigProvider
import com.kape.contracts.Router
import com.kape.data.LoginWithCredentials
import com.kape.data.TvWelcome
import com.kape.data.WebDestination
import com.kape.payments.SubscriptionPrefs
import com.kape.payments.domain.GetSubscriptionsUseCase
import com.kape.payments.ui.VpnSubscriptionPaymentProvider
import com.kape.payments.utils.PurchaseState
import com.kape.permissions.utils.PermissionUtil
import com.kape.signup.domain.ConsentUseCase
import com.kape.signup.domain.SignupUseCase
import com.kape.signup.utils.AMAZON_LOGIN
import com.kape.signup.utils.CONSENT
import com.kape.signup.utils.DEFAULT
import com.kape.signup.utils.EMAIL
import com.kape.signup.utils.ERROR_EMAIL_INVALID
import com.kape.signup.utils.ERROR_REGISTRATION
import com.kape.signup.utils.IN_PROCESS
import com.kape.signup.utils.LOADING
import com.kape.signup.utils.META_SUBSCRIPTIONS
import com.kape.signup.utils.NO_IN_APP_SUBSCRIPTIONS
import com.kape.signup.utils.Plan
import com.kape.signup.utils.SUBSCRIPTIONS
import com.kape.signup.utils.SUBSCRIPTIONS_FAILED_TO_LOAD
import com.kape.signup.utils.SignupScreenState
import com.kape.signup.utils.SubscriptionData
import com.kape.signup.utils.signedUp
import com.kape.ui.utils.PriceFormatter
import com.kape.utils.NetworkConnectionListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import java.util.Locale

@KoinViewModel
class SignupViewModel(
    private val router: Router,
    private val vpnSubscriptionPaymentProvider: VpnSubscriptionPaymentProvider,
    private val formatter: PriceFormatter,
    private val consentUseCase: ConsentUseCase,
    private val useCase: SignupUseCase,
    private val subscriptionPrefs: SubscriptionPrefs,
    private val subscriptionsUseCase: GetSubscriptionsUseCase,
    private val buildConfigProvider: BuildConfigProvider,
    private val permissionUtil: PermissionUtil,
    networkConnectionListener: NetworkConnectionListener,
) : ViewModel() {
    private var subscriptionData: SubscriptionData? = null
    private val _state = MutableStateFlow(DEFAULT)
    val state: StateFlow<SignupScreenState> = _state

    val isConnected = networkConnectionListener.isConnected

    init {
        viewModelScope.launch {
            vpnSubscriptionPaymentProvider.purchaseState.collect {
                when (it) {
                    PurchaseState.Default -> {
                        // no op
                    }

                    PurchaseState.InitFailed -> {
                        onProductsFailedToLoad()
                    }

                    PurchaseState.InitSuccess -> {
                        // no-op
                    }

                    PurchaseState.ProductsLoadedFailed -> {
                        onProductsFailedToLoad()
                    }

                    PurchaseState.ProductsLoadedSuccess -> {
                        val yearlyPlan =
                            vpnSubscriptionPaymentProvider.getFreeTrialYearlySubscriptionPlan()
                                ?: vpnSubscriptionPaymentProvider.getYearlySubscriptionPlan()
                        val monthlyPlan =
                            vpnSubscriptionPaymentProvider.getMonthlySubscriptionPlan()

                        if (yearlyPlan == null || monthlyPlan == null) {
                            onProductsFailedToLoad()
                            return@collect
                        }

                        val yearly =
                            Plan(
                                yearlyPlan.id,
                                yearlyPlan.plan.replaceFirstChar { first ->
                                    if (first.isLowerCase()) {
                                        first.titlecase(
                                            Locale.getDefault(),
                                        )
                                    } else {
                                        first.toString()
                                    }
                                },
                                true,
                                mainPrice = formatter.formatYearlyPlan(yearlyPlan.formattedPrice),
                                secondaryPrice = formatter.formatYearlyPerMonth(
                                    yearlyPlan.formattedPrice,
                                    yearlyPlan.currencyCode,
                                ),
                            )
                        val monthly = Plan(
                            monthlyPlan.id,
                            monthlyPlan.plan.replaceFirstChar { first ->
                                if (first.isLowerCase()) {
                                    first.titlecase(
                                        Locale.getDefault(),
                                    )
                                } else {
                                    first.toString()
                                }
                            },
                            false,
                            mainPrice = formatter.formatMonthlyPlan(monthlyPlan.formattedPrice),
                        )
                        val data = SubscriptionData(
                            mutableStateOf(yearly),
                            yearly,
                            monthly,
                        )
                        subscriptionData = data
                        _state.emit(
                            SUBSCRIPTIONS(data),
                        )
                    }

                    PurchaseState.PurchaseFailed -> {
                        // TODO: handle error
                    }

                    PurchaseState.PurchaseSuccess -> {
                        if (it == PurchaseState.PurchaseSuccess) {
                            _state.emit(CONSENT)
                        } else if (it == PurchaseState.PurchaseFailed) {
                            // TODO: handle error?
                            subscriptionData?.let { subscriptionData ->
                                _state.emit(SUBSCRIPTIONS(subscriptionData))
                            }
                        }
                    }

                    PurchaseState.NoInAppPurchase -> {
                        _state.emit(NO_IN_APP_SUBSCRIPTIONS)
                    }

                    PurchaseState.Disconnected -> {
                        // no-op
                    }
                }
            }
        }
    }

    fun loadPrices() = viewModelScope.launch {
        if (buildConfigProvider.isAmazonFlavor()) {
            _state.emit(AMAZON_LOGIN)
            return@launch
        }
        if (buildConfigProvider.isWebFlavor()) {
            _state.emit(NO_IN_APP_SUBSCRIPTIONS)
            return@launch
        }
        if (buildConfigProvider.isMetaFlavor()) {
            _state.emit(META_SUBSCRIPTIONS)
            return@launch
        }
        if (subscriptionPrefs.getVpnSubscriptions().isEmpty()) {
            _state.emit(LOADING)
            subscriptionsUseCase.getVpnSubscriptions()
            _state.emit(DEFAULT)
            vpnSubscriptionPaymentProvider.loadProducts()
        } else {
            vpnSubscriptionPaymentProvider.loadProducts()
        }
    }

    fun loadEmptyPrices() = viewModelScope.launch {
        _state.emit(SUBSCRIPTIONS_FAILED_TO_LOAD)
    }

    fun purchase(id: String) = viewModelScope.launch {
        vpnSubscriptionPaymentProvider.purchaseSelectedProduct(id)
        vpnSubscriptionPaymentProvider.reset()
    }

    fun navigateToLogin() {
        router.updateDestination(LoginWithCredentials)
        vpnSubscriptionPaymentProvider.reset()
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

    fun allowEventSharing(allow: Boolean) = viewModelScope.launch {
        // TODO: VPN-3101 - add kpi start/stop
        consentUseCase.setConsent(allow)
        _state.emit(EMAIL)
    }

    fun isValidEmail(email: String): Boolean =
        Patterns.EMAIL_ADDRESS.matcher(email).matches()

    fun register(email: String) = viewModelScope.launch {
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
        vpnSubscriptionPaymentProvider.purchaseState.value = PurchaseState.Default
    }

    fun registerClientIfNeeded(activity: Activity) {
        if (!vpnSubscriptionPaymentProvider.isClientRegistered()) {
            vpnSubscriptionPaymentProvider.register(activity)
        }
    }

    private fun onProductsFailedToLoad() = viewModelScope.launch {
        _state.emit(SUBSCRIPTIONS_FAILED_TO_LOAD)
    }
}