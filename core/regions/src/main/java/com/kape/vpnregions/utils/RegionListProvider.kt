package com.kape.vpnregions.utils

import com.kape.data.DI
import com.kape.data.vpnserver.VpnServer
import com.kape.vpnregions.data.VpnRegionRepository
import com.kape.vpnregions.domain.ReadVpnRegionsDetailsUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.Named
import java.util.Locale

class RegionListProvider(
    private val regionRepository: VpnRegionRepository,
    private val readVpnRegionsDetailsUseCase: ReadVpnRegionsDetailsUseCase,
    @Named(DI.IO_DISPATCHER) private val ioDispatcher: CoroutineDispatcher,
) {
    private val locale = Locale.getDefault().language
    private val isDefaultList = MutableStateFlow(true)
    private val _servers: MutableStateFlow<List<VpnServer>> = MutableStateFlow(emptyList())
    val servers: StateFlow<List<VpnServer>> = _servers

    init {
        setRegionsListToDefault()
    }

    fun isDefaultList(): Boolean = isDefaultList.value

    fun getOptimalServer(): VpnServer {
        if (_servers.value.isEmpty()) {
            setRegionsListToDefault()
        }
        val optimalServerOptions = _servers.value.filter { it.autoRegion && it.isGeo.not() }
        return if (optimalServerOptions.none { it.latency != null }) {
            optimalServerOptions.random()
        } else {
            optimalServerOptions.sortedBy { it.latency?.toInt() }.first()
        }
    }

    fun reflectDedicatedIpAction() {
        _servers.value = regionRepository.getServers(false)
    }

    fun loadVpnServerLatencies() = CoroutineScope(ioDispatcher).launch {
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
            _servers.value = regionRepository.getServers(isConnected)
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
        isDefaultList.value = false
        return updatedServers
    }

    private fun setRegionsListToDefault() {
        _servers.value = readVpnRegionsDetailsUseCase.readVpnRegionsDetailsFromAssetsFolder()
        isDefaultList.value = true
    }
}