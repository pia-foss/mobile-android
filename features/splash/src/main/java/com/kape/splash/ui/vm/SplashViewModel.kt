package com.kape.splash.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.payments.domain.GetSubscriptionsUseCase
import com.kape.router.ExitFlow
import com.kape.router.Router
import com.kape.vpnregions.utils.RegionListProvider
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

class SplashViewModel(
    private val useCase: GetSubscriptionsUseCase,
    private val router: Router,
    private val regionListProvider: RegionListProvider,
) : ViewModel(), KoinComponent {

    fun load() {
        if (regionListProvider.isDefaultList()) {
            regionListProvider.loadVpnServerLatencies()
        }
        viewModelScope.launch {
            useCase.getVpnSubscriptions().collect {
                router.handleFlow(ExitFlow.Splash)
            }
        }
    }
}