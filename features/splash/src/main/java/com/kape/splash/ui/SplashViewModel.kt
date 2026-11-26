package com.kape.splash.ui

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.contracts.IsUserLoggedInUseCase
import com.kape.contracts.Router
import com.kape.contracts.SubscriptionBilling
import com.kape.contracts.di.Qualifiers
import com.kape.data.Update
import com.kape.featureflags.domain.usecase.RefreshFeatureFlagsUseCase
import com.kape.regions.vpn.domain.usecase.ObserveIsDefaultVpnServerListUseCase
import com.kape.regions.vpn.domain.usecase.RefreshVpnServersUseCase
import com.kape.splash.domain.usecase.GetRequiredUpdateUrlUseCase
import com.kape.splash.domain.usecase.ResolveSplashDestinationUseCase
import com.kape.utils.PlatformUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Named
import java.util.Locale

@KoinViewModel
class SplashViewModel(
    private val router: Router,
    private val platformUtils: PlatformUtils,
    private val isUserLoggedIn: IsUserLoggedInUseCase,
    private val subscriptionBilling: SubscriptionBilling,
    private val refreshFeatureFlags: RefreshFeatureFlagsUseCase,
    private val getRequiredUpdateUrl: GetRequiredUpdateUrlUseCase,
    private val resolveSplashDestination: ResolveSplashDestinationUseCase,
    private val observeIsDefaultVpnServerList: ObserveIsDefaultVpnServerListUseCase,
    private val refreshVpnServers: RefreshVpnServersUseCase,
    @Named(Qualifiers.APP_SCOPE) private val appScope: CoroutineScope,
) : ViewModel() {
    private val featureFlagsRefresh: Job = viewModelScope.launch { refreshFeatureFlags() }

    private val isLoggedIn: Deferred<Boolean> by lazy { viewModelScope.async { isUserLoggedIn.invoke() } }

    init {
        viewModelScope.launch {
            if (shouldPreloadPrices()) subscriptionBilling.initialize()
        }
    }

    fun load(activity: Activity?) {
        // In the app scope, not viewModelScope: splash closes quickly, and the refresh
        // should finish so the next screens get the network list and latencies.
        appScope.launch {
            if (observeIsDefaultVpnServerList().first()) {
                refreshVpnServers(locale = Locale.getDefault().language, isConnected = false)
            }
        }

        viewModelScope.launch {
            featureFlagsRefresh.join()
            if (getRequiredUpdateUrl() != null) {
                router.updateDestination(Update)
                return@launch
            }
            if (activity != null && shouldPreloadPrices()) {
                subscriptionBilling.awaitReady(activity)
            }
            router.updateDestination(resolveSplashDestination(isLoggedIn.await(), platformUtils.isTv()))
        }
    }

    suspend fun shouldPreloadPrices(): Boolean = !isLoggedIn.await()

    fun loadPrices(activity: Activity) = subscriptionBilling.loadPrices(activity)
}