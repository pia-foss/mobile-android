package com.kape.splash.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.featureflags.domain.ForceUpdateUseCase
import com.kape.httpclient.domain.GetWebsiteDownloadLink
import com.kape.router.EnterFlow
import com.kape.router.ExitFlow
import com.kape.router.Router
import com.kape.vpnconnect.domain.ConnectionUseCase
import com.kape.vpnregions.utils.RegionListProvider
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

class SplashViewModel(
    private val router: Router,
    private val regionListProvider: RegionListProvider,
    private val forceUpdateUseCase: ForceUpdateUseCase,
    private val getWebsiteDownloadLink: GetWebsiteDownloadLink,
    private val appUpdateUrl: String,
    private val connectionUseCase: ConnectionUseCase,
) : ViewModel(), KoinComponent {

    private var updateUrl: String = ""

    fun load() {
        if (regionListProvider.isDefaultList()) {
            regionListProvider.loadVpnServerLatencies()
        }
        viewModelScope.launch {
            forceUpdateUseCase.requiresForceUpdate().collect { requiresUpdate ->
                if (requiresUpdate) {
                    viewModelScope.launch {
                        getWebsiteDownloadLink.invoke().collect {
                            updateUrl = it
                            if (updateUrl.isEmpty()) {
                                router.handleFlow(ExitFlow.Splash)
                            } else {
                                router.handleFlow(EnterFlow.Update)
                            }
                        }
                    }
                } else {
                    router.handleFlow(ExitFlow.Splash)
                }
            }
        }
    }

    fun onUpdateClicked(launchUpdate: (updateUrl: String) -> Unit) {
        viewModelScope.launch {
            connectionUseCase.stopConnection().collect {}
        }
        launchUpdate(appUpdateUrl.ifEmpty { updateUrl })
    }

    fun isConnected(): Boolean = connectionUseCase.isConnected()
}