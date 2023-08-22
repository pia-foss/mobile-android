package com.kape.signup.ui.vm

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.login.domain.GetUserLoggedInUseCase
import com.kape.payments.ui.PaymentProvider
import com.kape.payments.utils.PurchaseState
import com.kape.router.EnterFlow
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
import com.kape.signup.utils.LOADING
import com.kape.signup.utils.Plan
import com.kape.signup.utils.PriceFormatter
import com.kape.signup.utils.SignupScreenState
import com.kape.signup.utils.SubscriptionData
import com.kape.signup.utils.signedUp
import com.kape.signup.utils.subscriptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import java.util.*

class SignupViewModel(
    private val paymentProvider: PaymentProvider,
    private val formatter: PriceFormatter,
    private val consentUseCase: ConsentUseCase,
    private val useCase: SignupUseCase,
    private val getUserLoggedInUseCase: GetUserLoggedInUseCase,
    private val router: Router,
) : ViewModel(), KoinComponent {

    private val _state = MutableStateFlow(DEFAULT)
    val state: StateFlow<SignupScreenState> = _state

    private var data: SubscriptionData? = null

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
                            // TODO: handle error?
                        }

                        PurchaseState.InitSuccess -> {
                            // no-op
                        }

                        PurchaseState.ProductsLoadedFailed -> {
                            // TODO: handle error
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
                                        formatter.formatYearlyPerMonth(formattedPrice)
                                    } ?: run {
                                        formatter.formatYearlyPlan(yearlyPlan.price)
                                        formatter.formatYearlyPerMonth(yearlyPlan.price)
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
                                    formatter.formatMonthlyPlan(monthlyPlan.price)
                                },
                            )
                            data = SubscriptionData(mutableStateOf(yearly), yearly, monthly)
                            data?.let { subscriptionData ->
                                _state.emit(subscriptions(subscriptionData))
                            }
                        }

                        PurchaseState.PurchaseFailed -> {
                            // TODO: handle error
                        }

                        PurchaseState.PurchaseSuccess -> {
                            if (it == PurchaseState.PurchaseSuccess) {
                                _state.emit(CONSENT)
                            } else if (it == PurchaseState.PurchaseFailed) {
                                // TODO: handle error?
                                data?.let { subscriptionData ->
                                    _state.emit(subscriptions(subscriptionData))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun loadPrices() = viewModelScope.launch {
        _state.emit(LOADING)
        paymentProvider.loadProducts()
    }

    fun purchase(id: String) = viewModelScope.launch {
        _state.emit(LOADING)
        paymentProvider.purchaseSelectedProduct(id)
    }

    fun navigateToLogin() {
        router.handleFlow(EnterFlow.Login)
    }

    fun allowEventSharing(allow: Boolean) = viewModelScope.launch {
        // TODO: VPN-3101 - add kpi start/stop
        consentUseCase.setConsent(allow)
        _state.emit(EMAIL)
    }

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
}