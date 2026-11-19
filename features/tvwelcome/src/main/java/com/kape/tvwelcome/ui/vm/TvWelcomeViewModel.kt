package com.kape.tvwelcome.ui.vm

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.contracts.Router
import com.kape.contracts.SubscriptionBilling
import com.kape.data.TvLoginUsername
import com.kape.data.TvSubscribe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class TvWelcomeViewModel(
    private val router: Router,
    private val billing: SubscriptionBilling,
) : ViewModel() {
    private val _shouldShowSubscribeButton = MutableStateFlow(billing.isAvailable())
    val shouldShowSubscribeButton: StateFlow<Boolean> = _shouldShowSubscribeButton.asStateFlow()

    fun checkBillingAvailability(activity: Activity) {
        viewModelScope.launch {
            billing.awaitReady(activity)
            _shouldShowSubscribeButton.value = billing.isAvailable()
        }
    }

    fun login() = router.updateDestination(TvLoginUsername)

    fun signup() = router.updateDestination(TvSubscribe)
}