package com.kape.signup.ui.vm

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.payments.domain.BillingDataSource
import com.kape.payments.utils.PurchaseState
import com.kape.router.EnterFlow
import com.kape.router.Router
import com.kape.router.Subscribe
import com.kape.signup.utils.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*

class SubscribeViewModel(
    private val billingDataSource: BillingDataSource,
    private val formatter: PriceFormatter
) : ViewModel(),
    KoinComponent {

    private val router: Router by inject()
    private var data: SubscriptionData? = null
    private val _state = MutableStateFlow(IDLE)
    val state: StateFlow<SubscribeScreenState> = _state

    init {
        viewModelScope.launch {
            billingDataSource.purchaseState.collect {
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
                        val yearlyPlan = billingDataSource.getYearlySubscription()
                        val monthlyPlan = billingDataSource.getMonthlySubscription()
                        val yearly =
                            Plan(
                                yearlyPlan.id,
                                yearlyPlan.plan.replaceFirstChar { first -> if (first.isLowerCase()) first.titlecase(Locale.getDefault()) else first.toString() },
                                true,
                                mainPrice = formatter.formatYearlyPlan(yearlyPlan.formattedPrice!!),
                                formatter.formatYearlyPerMonth(yearlyPlan.formattedPrice!!)
                            )
                        val monthly = Plan(
                            monthlyPlan.id,
                            monthlyPlan.plan.replaceFirstChar { first -> if (first.isLowerCase()) first.titlecase(Locale.getDefault()) else first.toString() },
                            false,
                            mainPrice = formatter.formatMonthlyPlan(monthlyPlan.formattedPrice!!)
                        )
                        data = SubscriptionData(mutableStateOf(yearly), yearly, monthly)
                        data?.let { subscriptionData ->
                            _state.emit(loaded(subscriptionData))
                        }
                    }
                    PurchaseState.PurchaseFailed -> {
                        // TODO: handle error
                    }
                    PurchaseState.PurchaseSuccess -> {
                        if (it == PurchaseState.PurchaseSuccess) {
                            _state.emit(navigate(Subscribe.Consent))
                        } else if (it == PurchaseState.PurchaseFailed) {
                            // TODO: handle error?
                            data?.let { subscriptionData ->
                                _state.emit(loaded(subscriptionData))
                            }
                        }
                    }
                }
            }
        }
    }

    fun loadPrices() = viewModelScope.launch {
        _state.emit(LOADING)
        billingDataSource.loadProducts()
    }

    fun purchase(id: String) = viewModelScope.launch {
        _state.emit(SubscribeScreenState(idle = false, loading = true))
        billingDataSource.purchaseSelectedProduct(id)
    }

    fun navigateToLogin() {
        router.handleFlow(EnterFlow.Login)
    }
}