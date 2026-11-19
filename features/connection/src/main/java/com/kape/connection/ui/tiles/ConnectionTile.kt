package com.kape.connection.ui.tiles

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kape.connection.ui.vm.ConnectionTilesViewModel
import com.kape.customization.data.Element
import com.kape.data.ConnectionStatus
import com.kape.sharedui.tiles.ConnectionInfo
import com.kape.sharedui.tiles.IPTile
import com.kape.sharedui.tiles.QuickConnect
import com.kape.sharedui.tiles.QuickSettings
import com.kape.sharedui.tiles.ShadowsocksLocationPicker
import com.kape.sharedui.tiles.Snooze
import com.kape.sharedui.tiles.Traffic
import com.kape.sharedui.tiles.VpnLocationPicker
import com.kape.ui.R
import com.kape.ui.mobile.elements.InfoCard

@Composable
fun ConnectionTile(
    element: Element,
    tiles: ConnectionTilesViewModel,
    actions: TileActions,
    modifier: Modifier = Modifier,
) {
    val status by tiles.status.collectAsStateWithLifecycle()
    val isConnected = status == ConnectionStatus.CONNECTED

    when (element) {
        Element.ConnectionInfo -> {
            val settings by tiles.protocolSettings.collectAsStateWithLifecycle()
            settings?.let {
                ConnectionInfo(
                    modifier = modifier,
                    connection = it.name,
                    port = it.port,
                    auth = it.auth,
                    transport = it.transport.value,
                    encryption = it.dataEncryption.value,
                    handshake = it.handshake,
                )
            }
        }

        Element.IpInfo -> {
            val publicIp by tiles.publicIp.collectAsStateWithLifecycle()
            val vpnIp by tiles.vpnIp.collectAsStateWithLifecycle()
            val isPortForwardingEnabled by tiles.isPortForwardingEnabled.collectAsStateWithLifecycle()
            val portForwardingStatus by tiles.portForwardingStatus.collectAsStateWithLifecycle()
            val port by tiles.port.collectAsStateWithLifecycle()
            IPTile(
                publicIp = publicIp,
                vpnIp = vpnIp,
                isPortForwardingEnabled = isPortForwardingEnabled,
                portForwardingStatus = portForwardingStatus,
                port = port,
                modifier = modifier,
            )
        }

        Element.QuickConnect -> {
            val servers by tiles.quickConnect.collectAsStateWithLifecycle()
            QuickConnect(
                modifier = modifier,
                servers = servers.associate { it.server to it.isFavorite },
                isConnected = isConnected,
                onClick = actions::onQuickConnect,
            )
        }

        Element.QuickSettings ->
            QuickSettings(
                modifier = modifier,
                onKillSwitchClick = actions::onKillSwitchClick,
                onAutomationClick = actions::onAutomationClick,
                onProtocolsClick = actions::onProtocolsClick,
            )

        Element.VpnRegionSelection -> {
            val displayed by tiles.displayedServer.collectAsStateWithLifecycle()
            displayed?.let {
                Column(modifier = modifier) {
                    VpnLocationPicker(
                        modifier = Modifier.testTag(":ConnectionScreen:VpnLocationPicker"),
                        server = it.server,
                        isConnected = isConnected,
                        isOptimal = it.isOptimal,
                        onClick = actions::onRegionClick,
                    )
                    if (it.showOptimalLocationInfo) {
                        Spacer(modifier = Modifier.height(8.dp))
                        InfoCard(
                            content = stringResource(id = R.string.optimal_location_hint),
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                        )
                    }
                }
            }
        }

        Element.ShadowsocksRegionSelection -> {
            val server by tiles.shadowsocksServer.collectAsStateWithLifecycle()
            server?.let {
                ShadowsocksLocationPicker(
                    modifier = modifier,
                    server = it,
                    isConnected = isConnected,
                    onClick = actions::onShadowsocksRegionClick,
                )
            }
        }

        Element.Snooze -> {
            val isSnoozeActive by tiles.isSnoozeActive.collectAsStateWithLifecycle()
            val minutes by tiles.minutesUntilResume.collectAsStateWithLifecycle()
            Snooze(
                modifier = modifier,
                active = isSnoozeActive,
                timeUntilResume = snoozeTimeLeft(minutes),
                onClick = { if (isConnected) actions.onSnooze(it) },
                onResumeClick = actions::onResumeSnooze,
            )
        }

        Element.Traffic -> {
            val download by tiles.download.collectAsStateWithLifecycle()
            val upload by tiles.upload.collectAsStateWithLifecycle()
            Traffic(download = download, upload = upload, modifier = modifier)
        }
    }
}

@Composable
private fun snoozeTimeLeft(minutes: Int): String =
    stringResource(id = if (minutes == 1) R.string.minute_to_format else R.string.minutes_to_format).format(minutes)