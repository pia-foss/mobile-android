package com.kape.signup.data

import android.app.Activity
import androidx.compose.runtime.mutableStateOf
import com.kape.payments.domain.GetSubscriptionsUseCase
import com.kape.payments.prefs.SubscriptionPrefs
import com.kape.payments.ui.VpnSubscriptionPaymentProvider
import com.kape.payments.utils.PurchaseState
import com.kape.signup.domain.SignupBillingHandler
import com.kape.signup.utils.CONSENT
import com.kape.signup.utils.LOADING
import com.kape.signup.utils.NO_IN_APP_SUBSCRIPTIONS
import com.kape.signup.utils.Plan
import com.kape.signup.utils.SUBSCRIPTIONS
import com.kape.signup.utils.SUBSCRIPTIONS_FAILED_TO_LOAD
import com.kape.signup.utils.SignupScreenState
import com.kape.signup.utils.SubscriptionData
import com.kape.ui.utils.PriceFormatter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale

class GoogleSignupBillingHandler(
    private val vpnSubscriptionPaymentProvider: VpnSubscriptionPaymentProvider,
    private val subscriptionPrefs: SubscriptionPrefs,
    private val subscriptionsUseCase: GetSubscriptionsUseCase,
    private val formatter: PriceFormatter,
) : SignupBillingHandler {
    private val _billingState = MutableSharedFlow<SignupScreenState>(replay = 1)
    override val billingState: Flow<SignupScreenState> = _billingState
    private var subscriptionData: SubscriptionData? = null

    override fun initialize(
        scope: CoroutineScope,
        dispatcher: CoroutineDispatcher,
    ) {
        scope.launch(dispatcher) {
            vpnSubscriptionPaymentProvider.purchaseState.collect {
                when (it) {
                    PurchaseState.Default -> {}
                    PurchaseState.InitFailed -> _billingState.emit(SUBSCRIPTIONS_FAILED_TO_LOAD)
                    PurchaseState.InitSuccess -> {}
                    PurchaseState.ProductsLoadedFailed -> _billingState.emit(SUBSCRIPTIONS_FAILED_TO_LOAD)
                    PurchaseState.ProductsLoadedSuccess -> {
                        scope.launch(dispatcher) {
                            subscriptionPrefs.vpnSubscriptionPlans.collectLatest { _ ->
                                val yearlyPlan =
                                    vpnSubscriptionPaymentProvider.getFreeTrialYearlySubscriptionPlan()
                                        ?: vpnSubscriptionPaymentProvider.getYearlySubscriptionPlan()
                                val monthlyPlan = vpnSubscriptionPaymentProvider.getMonthlySubscriptionPlan()
                                if (yearlyPlan == null || monthlyPlan == null) {
                                    _billingState.emit(SUBSCRIPTIONS_FAILED_TO_LOAD)
                                    return@collectLatest
                                }
                                val yearly =
                                    Plan(
                                        yearlyPlan.id,
                                        yearlyPlan.plan.replaceFirstChar { first ->
                                            if (first.isLowerCase()) {
                                                first.titlecase(Locale.getDefault())
                                            } else {
                                                first.toString()
                                            }
                                        },
                                        true,
                                        mainPrice = formatter.formatYearlyPlan(yearlyPlan.formattedPrice),
                                        secondaryPrice =
                                            formatter.formatYearlyPerMonth(
                                                yearlyPlan.formattedPrice,
                                                yearlyPlan.currencyCode,
                                            ),
                                    )
                                val monthly =
                                    Plan(
                                        monthlyPlan.id,
                                        monthlyPlan.plan.replaceFirstChar { first ->
                                            if (first.isLowerCase()) {
                                                first.titlecase(Locale.getDefault())
                                            } else {
                                                first.toString()
                                            }
                                        },
                                        false,
                                        mainPrice = formatter.formatMonthlyPlan(monthlyPlan.formattedPrice),
                                    )
                                val data =
                                    SubscriptionData(
                                        mutableStateOf(yearly),
                                        yearly,
                                        monthly,
                                    )
                                subscriptionData = data
                                _billingState.emit(SUBSCRIPTIONS(data))
                            }
                        }
                    }
                    PurchaseState.PurchaseFailed -> {
                        // TODO: handle error
                    }
                    PurchaseState.PurchaseSuccess -> _billingState.emit(CONSENT)
                    PurchaseState.NoInAppPurchase -> _billingState.emit(NO_IN_APP_SUBSCRIPTIONS)
                    PurchaseState.Disconnected -> {}
                }
            }
        }
    }

    override fun loadPrices(
        scope: CoroutineScope,
        dispatcher: CoroutineDispatcher,
    ) {
        scope.launch(dispatcher) {
            _billingState.emit(LOADING)
            subscriptionsUseCase.getVpnSubscriptions()
            vpnSubscriptionPaymentProvider.loadProducts()
        }
    }

    override fun purchase(id: String) {
        vpnSubscriptionPaymentProvider.purchaseSelectedProduct(id)
    }

    override fun registerClientIfNeeded(activity: Activity) {
        if (!vpnSubscriptionPaymentProvider.isClientRegistered()) {
            vpnSubscriptionPaymentProvider.register(activity)
        }
    }

    override fun reset() {
        vpnSubscriptionPaymentProvider.reset()
    }
}