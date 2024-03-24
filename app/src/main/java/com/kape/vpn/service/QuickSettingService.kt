package com.kape.vpn.service

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.kape.login.domain.mobile.GetUserLoggedInUseCase
import com.kape.ui.R
import com.kape.vpnconnect.utils.ConnectionManager
import com.kape.vpnlauncher.VpnLauncher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.coroutines.CoroutineContext

class QuickSettingService : TileService(), KoinComponent, CoroutineScope {

    private val connectionManager: ConnectionManager by inject()
    private val vpnLauncher: VpnLauncher by inject()
    private val getUserLoggedInUseCase: GetUserLoggedInUseCase by inject()

    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.IO

    init {
        launch {
            connectionManager.connectionStatus.collect {
                updateTile()
            }
        }
    }

    override fun onClick() {
        super.onClick()
        if (isLocked) {
            unlockAndRun {
                onClickAction()
            }
        } else {
            onClickAction()
        }
    }

    override fun onStartListening() {
        super.onStartListening()
        updateTile()
    }

    private fun onClickAction() {
        if (getUserLoggedInUseCase.isUserLoggedIn()) {
            if (vpnLauncher.isVpnConnected()) {
                vpnLauncher.stopVpn()
            } else {
                vpnLauncher.launchVpn()
            }
        }
    }

    private fun updateTile() {
        if (connectionManager.isConnected()) {
            qsTile.state = Tile.STATE_ACTIVE
            qsTile.label = if (connectionManager.serverName.value.isEmpty()) {
                getString(R.string.qs_disconnect_nolocation)
            } else {
                getString(R.string.qs_disconnect, connectionManager.serverName.value)
            }
        } else {
            if (!getUserLoggedInUseCase.isUserLoggedIn()) {
                qsTile.state = Tile.STATE_UNAVAILABLE
                qsTile.label = getString(R.string.not_logged_in)
            } else {
                qsTile.state = Tile.STATE_INACTIVE
                qsTile.label = getString(R.string.qs_title)
            }
        }
        qsTile.updateTile()
    }
}