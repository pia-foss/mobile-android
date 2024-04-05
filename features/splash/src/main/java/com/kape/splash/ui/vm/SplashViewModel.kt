package com.kape.splash.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.payments.domain.GetSubscriptionsUseCase
import com.kape.router.ExitFlow
import com.kape.router.Router
import com.kape.vpnregions.utils.RegionListProvider
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import java.util.Locale

class SplashViewModel(
    private val useCase: GetSubscriptionsUseCase,
    private val router: Router,
    private val regionListProvider: RegionListProvider,
) : ViewModel(), KoinComponent {

    fun load() = viewModelScope.launch {
        useCase.getSubscriptions().collect {
            regionListProvider.updateServerLatencies(
                Locale.getDefault().language,
                isConnected = false,
                isUserInitiated = false,
            ).collect {
                router.handleFlow(ExitFlow.Splash)
            }
        }
    }
}