package com.kape.splash.ui.vm

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.payments.domain.BillingDataSource
import com.kape.payments.domain.GetSubscriptionsUseCase
import com.kape.payments.ui.BillingDataSourceImpl
import com.kape.router.ExitFlow
import com.kape.router.Router
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SplashViewModel(private val useCase: GetSubscriptionsUseCase, private val billingDataSource: BillingDataSource) : ViewModel(), KoinComponent {

    private val router: Router by inject()

    fun load(activity: Activity) = viewModelScope.launch {
        (billingDataSource as BillingDataSourceImpl).activity = activity
        billingDataSource.register()
        useCase.getSubscriptions().collect {
            router.handleFlow(ExitFlow.Splash)
        }
    }
}