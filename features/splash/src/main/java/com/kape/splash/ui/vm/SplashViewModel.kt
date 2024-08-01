package com.kape.splash.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.featureflags.domain.ForceUpdateUseCase
import com.kape.router.ExitFlow
import com.kape.router.Router
import com.kape.vpnregions.utils.RegionListProvider
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

class SplashViewModel(
    private val router: Router,
    private val regionListProvider: RegionListProvider,
    private val forceUpdateUseCase: ForceUpdateUseCase,
) : ViewModel(), KoinComponent {

    fun load() {
        if (regionListProvider.isDefaultList()) {
            regionListProvider.loadVpnServerLatencies()
        }
        viewModelScope.launch {
            forceUpdateUseCase.requiresForceUpdate().collect {
                // TODO: Implement force update flow
                router.handleFlow(ExitFlow.Splash)
            }
        }
    }
}