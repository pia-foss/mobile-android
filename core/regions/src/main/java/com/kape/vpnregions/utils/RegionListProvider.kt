package com.kape.vpnregions.utils

import com.kape.data.DI
import com.kape.data.vpnserver.VpnServer
import com.kape.vpnregions.data.VpnRegionRepository
import com.kape.vpnregions.domain.ReadVpnRegionsDetailsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.Named
import java.util.Locale

class RegionListProvider(
    private val regionRepository: VpnRegionRepository,
    private val readVpnRegionsDetailsUseCase: ReadVpnRegionsDetailsUseCase,
    @Named(DI.IO_SCOPE) private val ioScope: CoroutineScope,
) {
    private val locale = Locale.getDefault().language
    private val _isDefaultList = MutableStateFlow(true)
    val isDefaultList: StateFlow<Boolean> = _isDefaultList
    private val _servers: MutableStateFlow<List<VpnServer>> = MutableStateFlow(emptyList())
    val servers: StateFlow<List<VpnServer>> = _servers

    init {
        setRegionsListToDefault()
    }

    fun getOptimalServerOrNull(): VpnServer? {
        val servers = _servers.value
        if (servers.isEmpty()) return null
        val optimalServerOptions = servers.filter { it.autoRegion && it.isGeo.not() }
        return if (optimalServerOptions.none { it.latency != null }) {
            optimalServerOptions.randomOrNull() ?: servers.firstOrNull()
        } else {
            optimalServerOptions.sortedBy { it.latency?.toInt() }.firstOrNull()
        }
    }

    suspend fun getOptimalServer(): VpnServer {
        if (_servers.value.isEmpty()) {
            _servers.first { it.isNotEmpty() }
        }
        val optimalServerOptions = _servers.value.filter { it.autoRegion && it.isGeo.not() }
        return if (optimalServerOptions.none { it.latency != null }) {
            optimalServerOptions.randomOrNull() ?: _servers.value.first()
        } else {
            optimalServerOptions.sortedBy { it.latency?.toInt() }.first()
        }
    }

    suspend fun reflectDedicatedIpAction() {
        val nonDipServers = _servers.value.filter { !it.isDedicatedIp }
        _servers.value = regionRepository.addDipsToServerList(nonDipServers)
    }

    fun loadVpnServerLatencies() =
        ioScope.launch {
            updateServerLatencies(locale, false)
        }

    suspend fun updateServerLatencies(
        isConnected: Boolean,
        isUserInitiated: Boolean,
    ): List<VpnServer> {
        if (isUserInitiated) {
            _servers.value = updateServerLatencies(locale, isConnected)
        }
        return if (_servers.value.none { it.latency != null }) {
            val servers = updateServerLatencies(locale, isConnected)
            _servers.value = servers
            servers
        } else {
            _servers.value = regionRepository.getServers(isConnected).first()
            _servers.value
        }
    }

    private suspend fun updateServerLatencies(
        locale: String,
        isConnected: Boolean,
    ): List<VpnServer> {
        val regions = regionRepository.fetchVpnRegions(locale)
        val updatedServers = regionRepository.fetchLatencies(isConnected)
        for (server in updatedServers) {
            regions.filter { it.name == server.name }.firstOrNull()?.latency =
                server.latency ?: VPN_REGIONS_PING_TIMEOUT.toString()
        }
        _servers.value = updatedServers
        _isDefaultList.value = false
        return updatedServers
    }

    private fun setRegionsListToDefault() {
        ioScope.launch {
            val assetServers = readVpnRegionsDetailsUseCase.readVpnRegionsDetailsFromAssetsFolder()
            _servers.update { regionRepository.addDipsToServerList(assetServers) }
            _isDefaultList.update { true }
        }
    }
}