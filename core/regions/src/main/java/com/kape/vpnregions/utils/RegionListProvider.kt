package com.kape.vpnregions.utils

import com.kape.utils.vpnserver.VpnServer
import com.kape.vpnregions.data.VpnRegionRepository
import com.kape.vpnregions.domain.ReadVpnRegionsDetailsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow

class RegionListProvider(
    private val regionRepository: VpnRegionRepository,
    private val readVpnRegionsDetailsUseCase: ReadVpnRegionsDetailsUseCase,
) {

    private val _servers: MutableStateFlow<List<VpnServer>> = MutableStateFlow(emptyList())
    val servers: StateFlow<List<VpnServer>> = _servers

    init {
        setRegionsListToDefault()
    }

    fun setRegionsListToDefault() {
        _servers.value = readVpnRegionsDetailsUseCase.readVpnRegionsDetailsFromAssetsFolder()
    }

    fun getOptimalServer(): VpnServer {
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

    fun updateServerLatencies(locale: String, isConnected: Boolean, isUserInitiated: Boolean) =
        flow {
            if (isUserInitiated) {
                updateServerLatencies(locale, isConnected).collect {
                    _servers.value = it
                    emit(servers.value)
                }
            }
            if (_servers.value.none { it.latency != null }) {
                updateServerLatencies(locale, isConnected).collect {
                    _servers.value = it
                    emit(servers.value)
                }
            } else {
                _servers.value = regionRepository.getServers(isConnected)
                emit(servers.value)
            }
        }

    private fun updateServerLatencies(locale: String, isConnected: Boolean) = flow {
        regionRepository.fetchVpnRegions(locale).collect {
            regionRepository.fetchLatencies(isConnected)
                .collect { updatedServers ->
                    for (server in updatedServers) {
                        it.filter { it.name == server.name }[0].latency =
                            server.latency ?: VPN_REGIONS_PING_TIMEOUT.toString()
                    }
                    _servers.value = updatedServers
                    emit(updatedServers)
                }
        }
    }
}