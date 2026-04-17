package com.kape.vpnregions.utils

import com.kape.data.DI
import com.kape.data.shadowsocksserver.ShadowsocksServer
import com.kape.shadowsocksregions.domain.GetShadowsocksRegionsUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.Named
import java.util.Locale

class ShadowsocksListProvider(
    private val getShadowsocksRegionsUseCase: GetShadowsocksRegionsUseCase,
    @Named(DI.IO_DISPATCHER) private val ioDispatcher: CoroutineDispatcher,
) {
    private val locale = Locale.getDefault().language
    private val _servers: MutableStateFlow<List<ShadowsocksServer>> =
        MutableStateFlow(getShadowsocksRegionsUseCase.getShadowsocksServers())
    val servers = _servers.asStateFlow()

    init {
        getServers()
    }

    fun getSelected(): ShadowsocksServer =
        getShadowsocksRegionsUseCase.getSelectedShadowsocksServer()

    private fun getServers() {
        CoroutineScope(ioDispatcher).launch {
            val servers = getShadowsocksRegionsUseCase.fetchShadowsocksServers(locale)
            _servers.update { servers }
        }
    }
}