package com.kape.splash.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.payments.domain.BillingDataSource
import com.kape.payments.domain.GetSubscriptionsUseCase
import com.kape.router.ExitFlow
import com.kape.router.Router
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SplashViewModel(private val useCase: GetSubscriptionsUseCase, private val billingDataSource: BillingDataSource) : ViewModel(), KoinComponent {

    private val router: Router by inject()

    fun load() = viewModelScope.launch {
        useCase.getSubscriptions().collect {
            billingDataSource.loadProducts()
            router.handleFlow(ExitFlow.Splash)
        }
    }
}