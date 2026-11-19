package com.kape.vpn.system

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.kape.contracts.ConnectionStatusSource
import com.kape.ui.R
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

open class VpnTileService :
    TileService(),
    KoinComponent {
    private val connectionStatus: ConnectionStatusSource by inject()
    private val getVpnTile: GetVpnTileUseCase by inject()
    private val toggleVpn: ToggleVpnFromTileUseCase by inject()

    private val scope = MainScope()
    private var listening: Job? = null

    override fun onStartListening() {
        super.onStartListening()
        listening = scope.launch { connectionStatus.status.collect { updateTile() } }
    }

    override fun onStopListening() {
        listening?.cancel()
        super.onStopListening()
    }

    override fun onClick() {
        super.onClick()
        val toggle = { scope.launch { toggleVpn() } }
        if (isLocked) unlockAndRun { toggle() } else toggle()
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private suspend fun updateTile() {
        val tile = qsTile ?: return
        when (val state = getVpnTile()) {
            VpnTile.LoggedOut -> {
                tile.state = Tile.STATE_UNAVAILABLE
                tile.label = getString(R.string.not_logged_in)
            }

            VpnTile.Disconnected -> {
                tile.state = Tile.STATE_INACTIVE
                tile.label = getString(R.string.qs_title)
            }

            is VpnTile.Connected -> {
                tile.state = Tile.STATE_ACTIVE
                tile.label =
                    if (state.serverName.isEmpty()) {
                        getString(
                            R.string.qs_disconnect_nolocation,
                        )
                    } else {
                        getString(R.string.qs_disconnect, state.serverName)
                    }
            }
        }
        tile.updateTile()
    }
}