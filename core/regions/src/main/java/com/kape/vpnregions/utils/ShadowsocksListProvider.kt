package com.kape.vpnregions.utils

import com.kape.data.DI
import com.kape.data.shadowsocksserver.ShadowsocksServer
import com.kape.shadowsocksregions.domain.GetShadowsocksRegionsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.Named
import java.util.Locale

class ShadowsocksListProvider(
    private val getShadowsocksRegionsUseCase: GetShadowsocksRegionsUseCase,
    @Named(DI.IO_SCOPE) private val ioScope: CoroutineScope,
) {
    private val locale = Locale.getDefault().language
    private val _servers =
        getShadowsocksRegionsUseCase
            .getShadowsocksServers()
            .stateIn(
                scope = ioScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = emptyList(),
            )
    val servers: StateFlow<List<ShadowsocksServer>> = _servers
    val selectedServer: StateFlow<ShadowsocksServer?> =
        getShadowsocksRegionsUseCase.getSelectedShadowsocksServer().stateIn(
            scope = ioScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = null,
        )

    init {
        ioScope.launch {
            getShadowsocksRegionsUseCase.fetchShadowsocksServers(locale)
        }
    }
}