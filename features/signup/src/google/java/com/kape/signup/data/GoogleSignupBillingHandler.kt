package com.kape.signup.data

import android.app.Activity
import androidx.compose.runtime.mutableStateOf
import com.kape.localprefs.prefs.ConsentPrefs
import com.kape.payments.domain.GetSubscriptionsUseCase
import com.kape.payments.prefs.SubscriptionPrefs
import com.kape.payments.ui.VpnSubscriptionPaymentProvider
import com.kape.payments.utils.PurchaseState
import com.kape.shareevents.data.KpiEventGenerator
import com.kape.shareevents.domain.SubmitKpiEventUseCase
import com.kape.signup.domain.SignupBillingHandler
import com.kape.signup.utils.CONSENT
import com.kape.signup.utils.EMAIL
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
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds

private const val BILLING_READY_TIMEOUT_MS = 5_000L

class GoogleSignupBillingHandler(
    private val vpnSubscriptionPaymentProvider: VpnSubscriptionPaymentProvider,
    private val subscriptionPrefs: SubscriptionPrefs,
    private val subscriptionsUseCase: GetSubscriptionsUseCase,
    private val formatter: PriceFormatter,
    private val submitEventUseCase: SubmitKpiEventUseCase,
    private val consentPrefs: ConsentPrefs,
    private val eventGenerator: KpiEventGenerator,
    private val ioScope: CoroutineScope,
) : SignupBillingHandler {
    private val _billingState = MutableSharedFlow<SignupScreenState>(replay = 1)
    override val billingState: SharedFlow<SignupScreenState> = _billingState
    private var subscriptionData: SubscriptionData? = null
    private var initialized = false

    @Synchronized
    override fun initialize(mainDispatcher: CoroutineDispatcher) {
        if (!initialized) {
            initialized = true
            ioScope.launch {
                vpnSubscriptionPaymentProvider.purchaseState.collect {
                    when (it) {
                        PurchaseState.Default -> {}
                        PurchaseState.InitFailed -> _billingState.emit(SUBSCRIPTIONS_FAILED_TO_LOAD)
                        PurchaseState.InitSuccess -> {}
                        PurchaseState.ProductsLoadedFailed ->
                            _billingState.emit(
                                SUBSCRIPTIONS_FAILED_TO_LOAD,
                            )

                        PurchaseState.ProductsLoadedSuccess -> {
                            ioScope.launch {
                                subscriptionPrefs.vpnSubscriptionPlans.collectLatest { plans ->
                                    val yearlyPlan =
                                        vpnSubscriptionPaymentProvider.getFreeTrialYearlySubscriptionPlan()
                                            ?: vpnSubscriptionPaymentProvider.getYearlySubscriptionPlan()
                                    val monthlyPlan =
                                        vpnSubscriptionPaymentProvider.getFreeTrialMonthlySubscriptionPlan()
                                            ?: vpnSubscriptionPaymentProvider.getMonthlySubscriptionPlan()
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
                                            hasFreeTrial =
                                                yearlyPlan.freeTrialDuration?.isNotBlank()
                                                    ?: false,
                                            mainPrice = yearlyPlan.formattedPrice,
                                            secondaryPrice =
                                                formatter.formatYearlyPerMonth(
                                                    yearlyPlan.priceInMicros,
                                                    yearlyPlan.currencyCode,
                                                    yearlyPlan.formattedPrice,
                                                ),
                                            freeTrialDuration = yearlyPlan.freeTrialDuration,
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
                                            hasFreeTrial =
                                                monthlyPlan.freeTrialDuration?.isNotBlank()
                                                    ?: false,
                                            mainPrice = monthlyPlan.formattedPrice,
                                            freeTrialDuration = monthlyPlan.freeTrialDuration,
                                        )
                                    val data =
                                        withContext(mainDispatcher) {
                                            SubscriptionData(
                                                mutableStateOf(yearly),
                                                yearly,
                                                monthly,
                                            )
                                        }
                                    subscriptionData = data
                                    _billingState.emit(SUBSCRIPTIONS(data))
                                }
                            }
                        }

                        is PurchaseState.PurchaseFailed -> {
                            submitEventUseCase.submitEvent(eventGenerator.getProcessingFailure(it.reason))
                        }

                        PurchaseState.PurchaseSuccess -> {
                            submitEventUseCase.submitEvent(eventGenerator.getProcessingSuccess())
                            if (consentPrefs.hasMadeConsentDecision.first()) {
                                _billingState.emit(EMAIL)
                            } else {
                                _billingState.emit(CONSENT)
                            }
                        }

                        PurchaseState.NoInAppPurchase -> _billingState.emit(NO_IN_APP_SUBSCRIPTIONS)
                        PurchaseState.Disconnected -> {}
                    }
                }
            }
        }
    }

    override fun loadPrices(
        mainDispatcher: CoroutineDispatcher,
        activity: Activity,
    ) {
        ioScope.launch {
            withContext(mainDispatcher) {
                registerClientIfNeeded(activity)
            }
            _billingState.emit(LOADING)
            subscriptionsUseCase.getVpnSubscriptions()
            vpnSubscriptionPaymentProvider.loadProducts()
        }
    }

    override fun purchase(
        id: String,
        activity: Activity,
    ) {
        vpnSubscriptionPaymentProvider.purchaseSelectedProduct(id, activity)
    }

    override fun registerClientIfNeeded(activity: Activity) {
        if (!vpnSubscriptionPaymentProvider.isClientRegistered()) {
            vpnSubscriptionPaymentProvider.register(activity)
        }
    }

    override fun reset() {
        vpnSubscriptionPaymentProvider.reset()
    }

    override suspend fun hasResumablePurchase(): Boolean = subscriptionPrefs.getVpnPurchaseDataOnce() != null

    override fun hasActiveSubscription(): Flow<Boolean> = vpnSubscriptionPaymentProvider.hasActiveSubscription()

    override fun isBillingAvailable(): Boolean = vpnSubscriptionPaymentProvider.isClientRegistered()

    override suspend fun registerAndAwaitReady(
        mainDispatcher: CoroutineDispatcher,
        activity: Activity,
    ) {
        withContext(mainDispatcher) {
            registerClientIfNeeded(activity)
        }
        withTimeoutOrNull(BILLING_READY_TIMEOUT_MS.milliseconds) {
            vpnSubscriptionPaymentProvider.purchaseState.first {
                it == PurchaseState.InitSuccess || it == PurchaseState.InitFailed || it == PurchaseState.Disconnected
            }
        }
    }
}