package com.kape.splash.ui.vm

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.contracts.ConnectionInfoProvider
import com.kape.contracts.ConnectionManager
import com.kape.contracts.GetAppLatestVersion
import com.kape.contracts.IsUserLoggedInUseCase
import com.kape.contracts.Router
import com.kape.data.Connection
import com.kape.data.Consent
import com.kape.data.DI
import com.kape.data.Subscribe
import com.kape.data.TvConsent
import com.kape.data.TvWelcome
import com.kape.data.TvWelcomeBack
import com.kape.data.Update
import com.kape.data.WelcomeBack
import com.kape.featureflags.domain.FeatureFlagsDataSource
import com.kape.featureflags.domain.ForceUpdateUseCase
import com.kape.localprefs.prefs.ConsentPrefs
import com.kape.signup.domain.SignupBillingHandler
import com.kape.utils.PlatformUtils
import com.kape.vpnregions.utils.RegionListProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Named

@KoinViewModel
class SplashViewModel(
    private val router: Router,
    private val regionListProvider: RegionListProvider,
    private val forceUpdateUseCase: ForceUpdateUseCase,
    private val getAppLatestVersionUseCase: GetAppLatestVersion,
    @Named(DI.UPDATE_URL) private val appUpdateUrl: String,
    private val connectionManager: ConnectionManager,
    private val connectionInfoProvider: ConnectionInfoProvider,
    private val isUserLoggedIn: IsUserLoggedInUseCase,
    private val platformUtils: PlatformUtils,
    private val consentPrefs: ConsentPrefs,
    private val billingHandler: SignupBillingHandler,
    private val featureFlagsDataSource: FeatureFlagsDataSource,
    @Named(DI.IO_DISPATCHER) private val ioDispatcher: CoroutineDispatcher,
    @Named(DI.MAIN_DISPATCHER) private val mainDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private var updateUrl: String = ""
    private var hasMadeConsentDecision: Boolean = false
    private val featureFlagsFetchJob: Job

    init {
        featureFlagsFetchJob =
            viewModelScope.launch(ioDispatcher) {
                featureFlagsDataSource.invoke()
            }
        viewModelScope.launch {
            consentPrefs.hasMadeConsentDecision.collectLatest {
                hasMadeConsentDecision = it
            }
        }
        viewModelScope.launch(ioDispatcher) {
            if (shouldPreloadPrices()) {
                billingHandler.initialize(mainDispatcher)
            }
        }
    }

    fun load(activity: Activity?) {
        if (regionListProvider.isDefaultList.value) {
            regionListProvider.loadVpnServerLatencies()
        }
        viewModelScope.launch(ioDispatcher) {
            featureFlagsFetchJob.join()
            val requiresUpdate = forceUpdateUseCase.requiresForceUpdate()
            if (requiresUpdate) {
                val latestVersion = getAppLatestVersionUseCase.invoke()
                updateUrl = latestVersion?.url ?: ""
                if (updateUrl.isNotEmpty()) {
                    router.updateDestination(Update)
                    return@launch
                }
            }
            if (shouldPreloadPrices() && (activity != null)) {
                billingHandler.registerAndAwaitReady(mainDispatcher, activity)
            }
            handleSplashExit()
        }
    }

    fun onUpdateClicked(launchUpdate: (updateUrl: String) -> Unit) =
        viewModelScope.launch(ioDispatcher) {
            if (connectionManager.isConnectionInProgress()) {
                connectionManager.disconnect()
            }
            launchUpdate(appUpdateUrl.ifEmpty { updateUrl })
        }

    fun isConnected() = connectionInfoProvider.isConnected()

    suspend fun shouldPreloadPrices(): Boolean = !isUserLoggedIn.invoke()

    fun loadPrices(activity: Activity) {
        billingHandler.loadPrices(mainDispatcher, activity)
    }

    private suspend fun handleSplashExit() {
        if (isUserLoggedIn.invoke()) {
            router.updateDestination(Connection)
        } else {
            if (platformUtils.isTv()) {
                if (hasMadeConsentDecision) {
                    if (billingHandler.hasActiveSubscription().first()) {
                        router.updateDestination(TvWelcomeBack)
                    } else {
                        router.updateDestination(TvWelcome)
                    }
                } else {
                    router.updateDestination(TvConsent(true))
                }
            } else {
                if (hasMadeConsentDecision) {
                    if (billingHandler.hasActiveSubscription().first()) {
                        router.updateDestination(WelcomeBack)
                    } else {
                        router.updateDestination(Subscribe)
                    }
                } else {
                    router.updateDestination(Consent(true))
                }
            }
        }
    }
}