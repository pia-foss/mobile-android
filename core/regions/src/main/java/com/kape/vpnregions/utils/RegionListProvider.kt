package com.kape.vpnregions.utils

import com.kape.utils.vpnserver.VpnServer
import com.kape.vpnregions.data.VpnRegionRepository
import com.kape.vpnregions.domain.ReadVpnRegionsDetailsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.coroutines.CoroutineContext

class RegionListProvider(
    private val regionRepository: VpnRegionRepository,
    private val readVpnRegionsDetailsUseCase: ReadVpnRegionsDetailsUseCase,
) : CoroutineScope {
    private val locale = Locale.getDefault().language
    private val isDefaultList = MutableStateFlow(true)
    private val _servers: MutableStateFlow<List<VpnServer>> = MutableStateFlow(emptyList())
    val servers: StateFlow<List<VpnServer>> = _servers
    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.IO

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

    fun loadVpnServerLatencies() =
        launch {
            updateServerLatencies(locale, false).collect {
                job.complete()
            }
        }

    fun updateServerLatencies(
        isConnected: Boolean,
        isUserInitiated: Boolean,
    ) = flow {
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

    private fun updateServerLatencies(
        locale: String,
        isConnected: Boolean,
    ) = flow {
//        regionRepository.fetchVpnRegions(locale).collect {
//            regionRepository
//                .fetchLatencies(isConnected)
//                .collect { updatedServers ->
//                    for (server in updatedServers) {
//                        it.filter { it.name == server.name }[0].latency =
//                            server.latency ?: VPN_REGIONS_PING_TIMEOUT.toString()
//                    }
//                    _servers.value = updatedServers
//                    isDefaultList.value = false
//                    emit(updatedServers)
//                }
//        }
        emit(_servers.value)
    }

    private fun setRegionsListToDefault() {
        _servers.value = readVpnRegionsDetailsUseCase.readVpnRegionsDetailsFromAssetsFolder()
        isDefaultList.value = true
    }
}