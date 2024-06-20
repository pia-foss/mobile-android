package com.kape.splash.ui.vm

import androidx.lifecycle.ViewModel
import com.kape.router.ExitFlow
import com.kape.router.Router
import com.kape.vpnregions.utils.RegionListProvider
import org.koin.core.component.KoinComponent

class SplashViewModel(
    private val router: Router,
    private val regionListProvider: RegionListProvider,
) : ViewModel(), KoinComponent {

    fun load() {
        if (regionListProvider.isDefaultList()) {
            regionListProvider.loadVpnServerLatencies()
        }
        router.handleFlow(ExitFlow.Splash)
    }
}