package com.kape.signup.ui.vm

import android.util.Patterns
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.login.domain.mobile.GetUserLoggedInUseCase
import com.kape.payments.ui.PaymentProvider
import com.kape.payments.utils.PurchaseState
import com.kape.router.EnterFlow
import com.kape.router.Exit
import com.kape.router.ExitFlow
import com.kape.router.Router
import com.kape.signup.domain.ConsentUseCase
import com.kape.signup.domain.SignupUseCase
import com.kape.signup.utils.CONSENT
import com.kape.signup.utils.DEFAULT
import com.kape.signup.utils.EMAIL
import com.kape.signup.utils.ERROR_EMAIL_INVALID
import com.kape.signup.utils.ERROR_REGISTRATION
import com.kape.signup.utils.IN_PROCESS
import com.kape.signup.utils.NO_IN_APP_SUBSCRIPTIONS
import com.kape.signup.utils.Plan
import com.kape.signup.utils.PriceFormatter
import com.kape.signup.utils.SUBSCRIPTIONS
import com.kape.signup.utils.SignupScreenState
import com.kape.signup.utils.SignupStep
import com.kape.signup.utils.SubscriptionData
import com.kape.signup.utils.signedUp
import com.kape.utils.NetworkConnectionListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import java.util.Locale

class SignupViewModel(
    private val paymentProvider: PaymentProvider,
    private val formatter: PriceFormatter,
    private val consentUseCase: ConsentUseCase,
    private val useCase: SignupUseCase,
    private val getUserLoggedInUseCase: GetUserLoggedInUseCase,
    private val router: Router,
    networkConnectionListener: NetworkConnectionListener,
) : ViewModel(), KoinComponent {

    private val _state = MutableStateFlow(DEFAULT)
    val state: StateFlow<SignupScreenState> = _state
    val subscriptionData: MutableState<SubscriptionData?> = mutableStateOf(null)

    val isConnected = networkConnectionListener.isConnected

    init {
        viewModelScope.launch {
            if (getUserLoggedInUseCase.isUserLoggedIn()) {
                router.handleFlow(ExitFlow.Subscribe)
            } else {
                paymentProvider.purchaseState.collect {
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
                            val yearlyPlan = paymentProvider.getYearlySubscription()
                            val monthlyPlan = paymentProvider.getMonthlySubscription()
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
                                    mainPrice =
                                    yearlyPlan.formattedPrice?.let { formattedPrice ->
                                        formatter.formatYearlyPlan(formattedPrice)
                                    } ?: run {
                                        formatter.formatYearlyPlan(yearlyPlan.price.toString())
                                    },
                                    secondaryPrice = yearlyPlan.formattedPrice?.let { formattedPrice ->
                                        formatter.formatYearlyPerMonth(formattedPrice)
                                    } ?: run {
                                        formatter.formatYearlyPerMonth(yearlyPlan.price.toString())
                                    },
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
                                mainPrice = monthlyPlan.formattedPrice?.let { formattedPrice ->
                                    formatter.formatMonthlyPlan(formattedPrice)
                                } ?: run {
                                    formatter.formatMonthlyPlan(monthlyPlan.price.toString())
                                },
                            )
                            subscriptionData.value =
                                SubscriptionData(mutableStateOf(yearly), yearly, monthly)
                            _state.emit(SUBSCRIPTIONS)
                        }

                        PurchaseState.PurchaseFailed -> {
                            // TODO: handle error
                        }

                        PurchaseState.PurchaseSuccess -> {
                            if (it == PurchaseState.PurchaseSuccess) {
                                _state.emit(CONSENT)
                            } else if (it == PurchaseState.PurchaseFailed) {
                                // TODO: handle error?
                                subscriptionData.value?.let { subscriptionData ->
                                    _state.emit(SUBSCRIPTIONS)
                                }
                            }
                        }

                        PurchaseState.NoInAppPurchase -> {
                            _state.emit(NO_IN_APP_SUBSCRIPTIONS)
                        }
                    }
                }
            }
        }
    }

    fun loadPrices() = viewModelScope.launch {
        paymentProvider.loadProducts()
    }

    fun loadEmptyPrices() = viewModelScope.launch {
        _state.emit(SUBSCRIPTIONS)
    }

    fun purchase(id: String) = viewModelScope.launch {
        paymentProvider.purchaseSelectedProduct(id)
    }

    fun navigateToLogin() {
        router.handleFlow(EnterFlow.Login)
    }

    fun navigateToTvWelcome() {
        router.handleFlow(EnterFlow.TvWelcome)
    }

    fun navigateToPrivacyPolicy() {
        router.handleFlow(EnterFlow.PrivacyPolicy)
    }

    fun navigateToTermsOfService() {
        router.handleFlow(EnterFlow.TermsOfService)
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
        useCase.signup(email).collect {
            if (it == null) {
                _state.emit(ERROR_REGISTRATION)
            } else {
                _state.emit(signedUp(it))
            }
        }
    }

    fun completeSubscription() {
        router.handleFlow(ExitFlow.Subscribe)
        paymentProvider.purchaseState.value = PurchaseState.Default
    }

    fun exitApp() {
        router.handleFlow(Exit)
    }

    private fun onProductsFailedToLoad() = viewModelScope.launch {
        _state.emit(SignupScreenState(false, SignupStep.Subscriptions(true)))
    }
}