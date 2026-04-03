package com.kape.splash.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.contracts.IsUserLoggedInUseCase
import com.kape.contracts.Router
import com.kape.contracts.data.Connection
import com.kape.contracts.data.Subscribe
import com.kape.contracts.data.TvWelcome
import com.kape.contracts.data.Update
import com.kape.featureflags.domain.ForceUpdateUseCase
import com.kape.httpclient.domain.GetWebsiteDownloadLink
import com.kape.utils.DI
import com.kape.utils.PlatformUtils
import com.kape.vpnconnect.domain.StopConnectionUseCase
import com.kape.vpnconnect.utils.ConnectionInfoProvider
import com.kape.vpnregions.utils.RegionListProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Named
import org.koin.core.component.KoinComponent

@KoinViewModel
class SplashViewModel(
    private val router: Router,
    private val regionListProvider: RegionListProvider,
    private val forceUpdateUseCase: ForceUpdateUseCase,
    private val getWebsiteDownloadLink: GetWebsiteDownloadLink,
    @Named(DI.UPDATE_URL) private val appUpdateUrl: String,
    private val stopConnectionUseCase: StopConnectionUseCase,
    private val connectionInfoProvider: ConnectionInfoProvider,
    private val isUserLoggedIn: IsUserLoggedInUseCase,
    private val platformUtils: PlatformUtils,
    @Named(DI.IO_DISPATCHER) private val ioDispatcher: CoroutineDispatcher
) : ViewModel(){
    private var updateUrl: String = ""

    fun load() {
        if (regionListProvider.isDefaultList()) {
            regionListProvider.loadVpnServerLatencies()
        }
        viewModelScope.launch(ioDispatcher) {
            val requiresUpdate = forceUpdateUseCase.requiresForceUpdate()
            if (requiresUpdate) {
                val url = getWebsiteDownloadLink.invoke()
                updateUrl = url
                if (updateUrl.isNotEmpty()) {
                    router.updateDestination(Update)
                }
            } else if (!isUserLoggedIn.invoke()) {
                handleSplashExit()
            }
        }
        handleSplashExit()
    }

    fun onUpdateClicked(launchUpdate: (updateUrl: String) -> Unit) {
        viewModelScope.launch {
            stopConnectionUseCase()
        }
        launchUpdate(appUpdateUrl.ifEmpty { updateUrl })
    }

    fun isConnected() = connectionInfoProvider.isConnected()

    private fun handleSplashExit() {
        if (isUserLoggedIn.invoke()) {
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