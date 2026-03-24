package com.kape.splash.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.featureflags.domain.ForceUpdateUseCase
import com.kape.httpclient.domain.GetWebsiteDownloadLink
import com.kape.login.domain.mobile.GetUserLoggedInUseCase
import com.kape.router.Connection
import com.kape.router.Router
import com.kape.router.Subscribe
import com.kape.router.TvWelcome
import com.kape.router.Update
import com.kape.utils.PlatformUtils
import com.kape.vpnconnect.domain.ConnectionUseCase
import com.kape.vpnregions.utils.RegionListProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

class SplashViewModel(
    private val router: Router,
    private val regionListProvider: RegionListProvider,
    private val forceUpdateUseCase: ForceUpdateUseCase,
    private val getWebsiteDownloadLink: GetWebsiteDownloadLink,
    private val appUpdateUrl: String,
    private val connectionUseCase: ConnectionUseCase,
    private val getUserLoggedInUseCase: GetUserLoggedInUseCase,
    private val platformUtils: PlatformUtils,
) : ViewModel(), KoinComponent {

    private var updateUrl: String = ""

    fun load() {
        if (regionListProvider.isDefaultList()) {
            regionListProvider.loadVpnServerLatencies()
        }
        if (getUserLoggedInUseCase.isUserLoggedIn()) {
            handleSplashExit()
        }
        viewModelScope.launch(Dispatchers.IO) {
            forceUpdateUseCase.requiresForceUpdate().collect { requiresUpdate ->
                if (requiresUpdate) {
                    viewModelScope.launch {
                        getWebsiteDownloadLink.invoke().collect {
                            updateUrl = it
                            if (updateUrl.isNotEmpty()) {
                                router.updateDestination(Update)
                            }
                        }
                    }
                } else if (!getUserLoggedInUseCase.isUserLoggedIn()) {
                    handleSplashExit()
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

    private fun handleSplashExit() {
        if (getUserLoggedInUseCase.isUserLoggedIn()) {
            router.updateDestination(Connection)
        } else {
            if (platformUtils.isTv()) {
                router.updateDestination(TvWelcome)
            } else {
                router.updateDestination(Subscribe)
            }
        }
    }
}