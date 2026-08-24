package com.kape.vpnregionselection.ui.vm

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.contracts.ConnectionInfoProvider
import com.kape.contracts.ConnectionManager
import com.kape.contracts.Router
import com.kape.data.AUTO_KEY
import com.kape.data.Connection
import com.kape.data.DI
import com.kape.data.HelpSettings
import com.kape.data.RegionItemType
import com.kape.data.RegionServerItem
import com.kape.data.TvSideMenu
import com.kape.data.vpnserver.VpnServer
import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.localprefs.prefs.VpnRegionPrefs
import com.kape.regions.data.ServerData
import com.kape.settings.data.Transport
import com.kape.settings.data.VpnProtocols
import com.kape.utils.UpdateAvailableManager
import com.kape.utils.arrangeServers
import com.kape.utils.filterServersByName
import com.kape.vpnregions.utils.RegionListProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Named

@KoinViewModel
class VpnRegionSelectionViewModel(
    private val router: Router,
    private val regionListProvider: RegionListProvider,
    private val vpnRegionPrefs: VpnRegionPrefs,
    private val settingsPrefs: SettingsPrefs,
    private val connectionPrefs: ConnectionPrefs,
    private val connectionInfoProvider: ConnectionInfoProvider,
    private val connectionManager: ConnectionManager,
    private val updateAvailableManager: UpdateAvailableManager,
    @Named(DI.IO_DISPATCHER) private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    val servers = mutableStateOf(emptyList<RegionServerItem<VpnServer>>())
    val sorted = mutableStateOf(emptyList<RegionServerItem<VpnServer>>())
    private val _selectedServer = MutableStateFlow<VpnServer?>(null)
    val selectedServer = _selectedServer.asStateFlow()

    lateinit var autoRegionName: String
    lateinit var autoRegionIso: String

    val isPortForwardingEnabled = settingsPrefs.isPortForwardingEnabled
    val hasUpdateAvailable = updateAvailableManager.hasUpdateAvailable

    fun loadVpnRegions(
        locale: String,
        isLoading: MutableState<Boolean>,
        displayLoading: Boolean,
    ) = viewModelScope.launch(ioDispatcher) {
        arrangeVpnServers(regionListProvider.servers.value)
        if (displayLoading) {
            isLoading.value = true
        }
        val servers =
            regionListProvider.updateServerLatencies(isVpnConnectionActive(), displayLoading)
        arrangeVpnServers(servers)
        isLoading.value = false
    }

    fun selectServer(server: VpnServer?): Boolean {
        if (connectionPrefs.selectedVpnServer.value == server) {
            return false
        } else {
            _selectedServer.update { server }
            return true
        }
    }

    fun onVpnRegionSelected(server: VpnServer) {
        viewModelScope.launch(ioDispatcher) {
            val connectTo =
                if (server.endpoints.isEmpty()) {
                    regionListProvider.getOptimalServer()
                } else {
                    server
                }
            if (connectionManager.isConnectionInProgress()) {
                connectionManager.disconnect()
            }
            connectionManager.connect(
                connectTo,
                true,
                ::callback,
                {
                    // no-op for now, might be used for fallback
                },
            )
            router.navigateBack()
        }
    }

    private fun callback() {
        viewModelScope.launch(ioDispatcher) { connectionManager.disconnect() }
    }

    fun onFavoriteVpnClicked(serverData: ServerData) =
        viewModelScope.launch(ioDispatcher) {
            if (vpnRegionPrefs.isFavorite(serverData).first()) {
                vpnRegionPrefs.removeFromFavorites(serverData)
            } else {
                vpnRegionPrefs.addToFavorites(serverData)
            }
            arrangeVpnServers()
            updateVpnServers()
        }

    fun filterByName(
        value: String,
        isSearchEnabled: MutableState<Boolean>? = null,
    ) = viewModelScope.launch(ioDispatcher) {
        isSearchEnabled?.value = value.isNotEmpty()
        sorted.value =
            filterServersByName(servers.value, value) { item ->
                (item.type as? RegionItemType.Content)?.server?.name
            }
    }

    fun navigateToHelp() {
        router.updateDestination(HelpSettings)
    }

    fun navigateToSideMenu() {
        router.updateDestination(TvSideMenu)
    }

    fun navigateToVpn() {
        router.updateDestination(Connection)
    }

    fun getTvVpnServers(): MutableState<List<RegionServerItem<VpnServer>>> {
        var autoRegionIndex =
            servers.value.indexOfFirst { serverItem: RegionServerItem<VpnServer> ->
                val type = serverItem.type
                type is RegionItemType.Content &&
                    type.server.iso == autoRegionIso &&
                    type.server.name == autoRegionName
            }
        if (autoRegionIndex == -1) {
            autoRegionIndex = 0
        }
        return mutableStateOf(servers.value.subList(autoRegionIndex, servers.value.size))
    }

    fun getTvSearchVpnServers(): MutableState<List<RegionServerItem<VpnServer>>> {
        var autoRegionIndex =
            sorted.value.indexOfFirst { serverItem: RegionServerItem<VpnServer> ->
                val type = serverItem.type
                type is RegionItemType.Content &&
                    type.server.iso == autoRegionIso &&
                    type.server.name == autoRegionName
            }
        if (autoRegionIndex == -1) {
            autoRegionIndex = 0
        } else {
            autoRegionIndex += 1
        }
        return mutableStateOf(sorted.value.subList(autoRegionIndex, sorted.value.size))
    }

    fun isVpnConnectionActive(): Boolean = connectionInfoProvider.isConnected()

    private fun isVpnServerFavorite(serverData: ServerData) = vpnRegionPrefs.isFavorite(serverData)

    suspend fun arrangeVpnServers(items: List<VpnServer>? = null) {
        val serverGroup = mapProtocolToServerGroup()
        val showGeoLocatedServers = settingsPrefs.isShowGeoLocatedServersEnabled.first()
        val autoRegion = (getAutoRegion(autoRegionName, autoRegionIso).type as RegionItemType.Content).server

        val sourceServers = items ?: servers.value.mapNotNull { (it.type as? RegionItemType.Content)?.server }
        val sortedServers =
            sourceServers
                .filterNot { it.key == AUTO_KEY }
                .sortedWith(compareByDescending<VpnServer> { it.isDedicatedIp }.thenBy { it.latency?.toInt() })

        servers.value =
            arrangeServers(
                items = listOf(autoRegion) + sortedServers,
                currentItems = servers.value,
                toServer = { item -> (item.type as? RegionItemType.Content)?.server },
                isFavorite = { server -> isVpnServerFavorite(ServerData(server.name, server.isDedicatedIp)).first() },
                toItem = { server, favorite ->
                    RegionServerItem(
                        type =
                            RegionItemType.Content(
                                isFavorite = favorite,
                                enableFavorite = server.key != AUTO_KEY,
                                server = server,
                            ),
                    )
                },
                headingFavorites = RegionServerItem(type = RegionItemType.HeadingFavorites),
                headingAll = RegionServerItem(type = RegionItemType.HeadingAll),
                filter = { server ->
                    server.key == AUTO_KEY ||
                        (
                            (showGeoLocatedServers || server.isGeo.not()) &&
                                (serverGroup == null || server.endpoints[serverGroup].isNullOrEmpty().not())
                        )
                },
            )
    }

    private fun updateVpnServers() {
        val updatedList = mutableListOf<RegionServerItem<VpnServer>>()
        for (item in sorted.value) {
            val current = item.type as RegionItemType.Content<VpnServer>
            updatedList.add(
                servers.value.first {
                    val type = it.type
                    type is RegionItemType.Content && type.server.name == current.server.name
                },
            )
        }
        sorted.value = updatedList
    }

    private fun getAutoRegion(
        name: String,
        iso: String,
    ): RegionServerItem<VpnServer> =
        RegionServerItem(
            type =
                RegionItemType.Content(
                    isFavorite = false,
                    enableFavorite = false,
                    server =
                        VpnServer(
                            name = name,
                            iso = iso,
                            dns = "",
                            latency = null,
                            endpoints = emptyMap(),
                            key = AUTO_KEY,
                            latitude = null,
                            longitude = null,
                            isGeo = false,
                            allowsPortForwarding = false,
                            isOffline = false,
                            autoRegion = true,
                            dipToken = null,
                            dedicatedIp = null,
                        ),
                ),
        )

    private suspend fun mapProtocolToServerGroup(): VpnServer.ServerGroup? =
        when (settingsPrefs.getSelectedProtocolNow()) {
            VpnProtocols.WireGuard -> VpnServer.ServerGroup.WIREGUARD
            VpnProtocols.OpenVPN -> {
                when (settingsPrefs.openVpnSettings.value.transport) {
                    Transport.UDP -> VpnServer.ServerGroup.OPENVPN_UDP
                    Transport.TCP -> VpnServer.ServerGroup.OPENVPN_TCP
                    Transport.AUTO -> VpnServer.ServerGroup.OPENVPN_UDP // never used
                }
            }

            VpnProtocols.Automatic -> null
        }
}