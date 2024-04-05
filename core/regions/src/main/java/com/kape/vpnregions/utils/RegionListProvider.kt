package com.kape.vpnregions.utils

import com.kape.utils.vpnserver.VpnServer
import com.kape.vpnregions.domain.GetVpnRegionsUseCase
import com.kape.vpnregions.domain.ReadVpnRegionsDetailsUseCase
import com.kape.vpnregions.domain.UpdateLatencyUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow

class RegionListProvider(
    private val getVpnRegionsUseCase: GetVpnRegionsUseCase,
    private val updateLatencyUseCase: UpdateLatencyUseCase,
    readVpnRegionsDetailsUseCase: ReadVpnRegionsDetailsUseCase,
) {

    private val _servers: MutableStateFlow<List<VpnServer>> = MutableStateFlow(emptyList())
    val servers: StateFlow<List<VpnServer>> = _servers

    init {
        _servers.value = readVpnRegionsDetailsUseCase.readVpnRegionsDetailsFromAssetsFolder()
    }

    fun updateServerLatencies(locale: String, isConnected: Boolean, isUserInitiated: Boolean) =
        flow {
            if (isUserInitiated) {
                updateServerLatencies(locale, isConnected).collect {
                    emit(servers.value)
                }
            }
            if (_servers.value.none { it.latency != null }) {
                updateServerLatencies(locale, isConnected).collect {
                    emit(servers.value)
                }
            } else {
                emit(servers.value)
            }
        }

    private fun updateServerLatencies(locale: String, isConnected: Boolean) = flow {
        getVpnRegionsUseCase.loadVpnServers(locale).collect {
            updateLatencyUseCase.updateLatencies(isConnected)
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