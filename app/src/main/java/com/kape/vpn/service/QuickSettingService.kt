package com.kape.vpn.service

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.kape.contracts.IsUserLoggedInUseCase
import com.kape.ui.R
import com.kape.vpnconnect.utils.ConnectionManager
import com.kape.vpnlauncher.VpnLauncher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Singleton
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.coroutines.CoroutineContext

@Singleton
class QuickSettingService : TileService(), KoinComponent, CoroutineScope {

    private val connectionManager: ConnectionManager by inject()
    private val vpnLauncher: VpnLauncher by inject()
    private val isUserLoggedIn: IsUserLoggedInUseCase by inject()

    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.IO

    init {
        launch {
            connectionManager.connectionStatus.collect {
                withContext(Dispatchers.Main) {
                    updateTile()
                }
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
        if (isUserLoggedIn.invoke()) {
            if (vpnLauncher.isVpnConnected()) {
                vpnLauncher.stopVpn()
            } else {
                vpnLauncher.launchVpn()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    private fun updateTile() {
        qsTile?.let {
            if (connectionManager.isConnected()) {
                it.state = Tile.STATE_ACTIVE
                it.label = if (connectionManager.serverName.value.isEmpty()) {
                    getString(R.string.qs_disconnect_nolocation)
                } else {
                    getString(R.string.qs_disconnect, connectionManager.serverName.value)
                }
            } else {
                if (!isUserLoggedIn.invoke()) {
                    it.state = Tile.STATE_UNAVAILABLE
                    it.label = getString(R.string.not_logged_in)
                } else {
                    it.state = Tile.STATE_INACTIVE
                    it.label = getString(R.string.qs_title)
                }
            }
            it.updateTile()
        }
    }
}