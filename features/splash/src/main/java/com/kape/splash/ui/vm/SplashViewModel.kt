package com.kape.splash.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.notifications.data.NotificationPermissionManager
import com.kape.payments.domain.GetSubscriptionsUseCase
import com.kape.router.ExitFlow
import com.kape.router.Router
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

class SplashViewModel(
    private val useCase: GetSubscriptionsUseCase,
    private val notificationPermissionManager: NotificationPermissionManager,
    private val router: Router,
) : ViewModel(), KoinComponent {

    fun load() = viewModelScope.launch {
        useCase.getSubscriptions().collect {
            router.handleFlow(ExitFlow.Splash)
        }
    }

    fun isNotificationPermissionGranted(): Boolean =
        notificationPermissionManager.isNotificationsPermissionGranted()
}