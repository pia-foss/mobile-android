package com.kape.signup.ui.vm

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.payments.domain.BillingDataSource
import com.kape.payments.utils.PurchaseState
import com.kape.router.Router
import com.kape.signup.utils.Plan
import com.kape.signup.utils.PriceFormatter
import com.kape.signup.utils.SubscribeScreenState
import com.kape.signup.utils.SubscriptionData
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
    private val _state = MutableStateFlow(SubscribeScreenState(idle = true, loading = false))
    val state: StateFlow<SubscribeScreenState> = _state

    fun loadPrices() = viewModelScope.launch {
        _state.emit(SubscribeScreenState(idle = false, loading = true))
        billingDataSource.purchaseState.collect {
            if (it == PurchaseState.ProductsLoadedSuccess) {
                val yearlyPlan = billingDataSource.getYearlySubscription()
                val monthlyPlan = billingDataSource.getMonthlySubscription()
                val yearly =
                    Plan(
                        yearlyPlan.plan.replaceFirstChar { first -> if (first.isLowerCase()) first.titlecase(Locale.getDefault()) else first.toString() },
                        true,
                        mainPrice = formatter.formatYearlyPlan(yearlyPlan.formattedPrice!!),
                        formatter.formatYearlyPerMonth(yearlyPlan.formattedPrice!!)
                    )
                val monthly = Plan(
                    monthlyPlan.plan.replaceFirstChar { first -> if (first.isLowerCase()) first.titlecase(Locale.getDefault()) else first.toString() },
                    false,
                    mainPrice = formatter.formatMonthlyPlan(monthlyPlan.formattedPrice!!)
                )
                val data = SubscriptionData(mutableStateOf(yearly), yearly, monthly)
                _state.emit(SubscribeScreenState(idle = true, loading = false, data))
            }
        }
    }
}