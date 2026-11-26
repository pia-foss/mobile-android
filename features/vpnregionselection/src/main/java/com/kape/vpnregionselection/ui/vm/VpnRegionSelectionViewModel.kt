package com.kape.vpnregionselection.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.contracts.AppUpdateStatus
import com.kape.contracts.ConnectionInfoProvider
import com.kape.contracts.Router
import com.kape.data.Connection
import com.kape.data.HelpSettings
import com.kape.data.TvSideMenu
import com.kape.data.searchByName
import com.kape.data.vpnserver.VpnServer
import com.kape.localprefs.api.ConnectionStore
import com.kape.localprefs.api.SettingsStore
import com.kape.regions.vpn.domain.usecase.RefreshVpnServersUseCase
import com.kape.vpnregionselection.domain.usecase.ConnectToVpnRegionUseCase
import com.kape.vpnregionselection.domain.usecase.ObserveVpnRegionsUseCase
import com.kape.vpnregionselection.domain.usecase.ToggleFavoriteVpnRegionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class VpnRegionSelectionViewModel(
    observeVpnRegions: ObserveVpnRegionsUseCase,
    private val refreshVpnServers: RefreshVpnServersUseCase,
    private val toggleFavoriteVpnRegion: ToggleFavoriteVpnRegionUseCase,
    private val connectToVpnRegion: ConnectToVpnRegionUseCase,
    private val connectionInfo: ConnectionInfoProvider,
    private val connectionPrefs: ConnectionStore,
    private val router: Router,
    settingsPrefs: SettingsStore,
    appUpdateStatus: AppUpdateStatus,
) : ViewModel() {
    private val query = MutableStateFlow("")
    private val isRefreshing = MutableStateFlow(false)

    val state: StateFlow<VpnRegionsState> =
        combine(observeVpnRegions(), query, isRefreshing) { regions, query, isRefreshing ->
            VpnRegionsState(
                regions = regions,
                searchResults = query.takeIf { it.isNotEmpty() }?.let { regions.searchByName(it) { server -> server.name } },
                isRefreshing = isRefreshing,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), VpnRegionsState())

    val isPortForwardingEnabled: StateFlow<Boolean> = settingsPrefs.isPortForwardingEnabled
    val hasUpdateAvailable: StateFlow<Boolean> = appUpdateStatus.hasUpdateAvailable

    private val _pendingLocationChange = MutableStateFlow<VpnServer?>(null)

    val pendingLocationChange: StateFlow<VpnServer?> = _pendingLocationChange.asStateFlow()

    /** While connected, latencies are cleared instead, as pings would go through the VPN. */
    fun refresh(
        locale: String,
        force: Boolean,
    ) {
        viewModelScope.launch {
            isRefreshing.value = force
            try {
                refreshVpnServers(locale, connectionInfo.isConnected(), force)
            } finally {
                isRefreshing.value = false
            }
        }
    }

    fun search(query: String) {
        this.query.value = query
    }

    fun isConnected(): Boolean = connectionInfo.isConnected()

    fun onRegionClicked(region: VpnServer) {
        viewModelScope.launch {
            when {
                !connectionInfo.isConnected() -> connectTo(region)
                region == connectionPrefs.getSelectedVpnServerNow() -> router.navigateBack()
                else -> _pendingLocationChange.value = region
            }
        }
    }

    fun onLocationChangeConfirmed() {
        _pendingLocationChange.value?.let(::connectTo)
        _pendingLocationChange.value = null
    }

    fun onLocationChangeDismissed() {
        _pendingLocationChange.value = null
    }

    fun connectTo(region: VpnServer) {
        connectToVpnRegion(region)
        router.navigateBack()
    }

    fun onFavoriteClicked(region: VpnServer) {
        viewModelScope.launch { toggleFavoriteVpnRegion(region) }
    }

    fun navigateToHelp() = router.updateDestination(HelpSettings)

    fun navigateToSideMenu() = router.updateDestination(TvSideMenu)

    fun navigateToVpn() = router.updateDestination(Connection)
}

private const val STOP_TIMEOUT_MS = 5_000L