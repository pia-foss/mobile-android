package com.kape.connection.ui

import android.content.Context
import android.content.Intent
import android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.dp
import com.kape.appbar.view.AppBar
import com.kape.appbar.view.AppBarType
import com.kape.appbar.viewmodel.AppBarViewModel
import com.kape.connection.ui.vm.ConnectionViewModel
import com.kape.customization.data.Element
import com.kape.customization.data.ScreenElement
import com.kape.portforwarding.data.model.PortForwardingStatus
import com.kape.sidemenu.ui.SideMenu
import com.kape.ui.elements.Screen
import com.kape.ui.elements.Separator
import com.kape.ui.tiles.ConnectionInfo
import com.kape.ui.tiles.IPTile
import com.kape.ui.tiles.QuickConnect
import com.kape.ui.tiles.QuickSettings
import com.kape.ui.tiles.ShadowsocksLocationPicker
import com.kape.ui.tiles.Snooze
import com.kape.ui.tiles.Traffic
import com.kape.ui.tiles.VpnLocationPicker
import com.kape.ui.utils.connectivityState
import com.kape.utils.InternetConnectionState
import com.kape.vpnconnect.utils.ConnectionManager
import com.kape.vpnconnect.utils.ConnectionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import java.util.Locale

@OptIn(ExperimentalComposeUiApi::class, ExperimentalCoroutinesApi::class)
@Composable
fun ConnectionScreen() = Screen {
    val viewModel: ConnectionViewModel = koinViewModel()
    val appBarViewModel: AppBarViewModel = koinViewModel()
    val locale = Locale.getDefault().language
    val connectionManager: ConnectionManager = koinInject()
    val connection by connectivityState()
    val isConnected = connection === InternetConnectionState.Connected
    val connectionStatus = connectionManager.connectionStatus.collectAsState()
    val scope: CoroutineScope = rememberCoroutineScope()
    val drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed)

    BackHandler {
        viewModel.exitApp()
    }

    LaunchedEffect(key1 = Unit) {
        viewModel.loadVpnServers(locale)
        viewModel.loadShadowsocksServers(locale)
        viewModel.autoConnect()
    }

    SideMenu(scope, drawerState) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .semantics {
                    testTagsAsResourceId = true
                },
        ) {
            AppBar(
                viewModel = appBarViewModel,
                type = AppBarType.Connection,
                onLeftIconClick = {
                    scope.launch {
                        drawerState.open()
                    }
                },
                onRightIconClick = { viewModel.navigateToCustomization() },
            )
            Spacer(modifier = Modifier.height(16.dp))
            ConnectButton(
                if (isConnected) connectionStatus.value else ConnectionStatus.ERROR,
                Modifier
                    .align(CenterHorizontally)
                    .testTag(":ConnectionScreen:connection_button"),
            ) {
                viewModel.onConnectionButtonClicked()
            }
            Spacer(modifier = Modifier.height(36.dp))
            viewModel.getOrderedElements().forEach {
                DisplayComponent(
                    screenElement = it,
                    isVisible = viewModel.isScreenElementVisible(it),
                    viewModel = viewModel,
                )
                Separator()
            }
        }
    }
}

@Composable
private fun DisplayComponent(
    screenElement: ScreenElement,
    isVisible: Boolean,
    viewModel: ConnectionViewModel,
) {
    if (isVisible) {
        val context: Context = LocalContext.current
        when (screenElement.element) {
            Element.ConnectionInfo -> {
                val settings = viewModel.getConnectionSettings()
                ConnectionInfo(
                    connection = settings.name,
                    port = settings.port,
                    auth = settings.auth,
                    transport = settings.transport.value,
                    encryption = settings.dataEncryption.value,
                    handshake = settings.handshake,
                )
            }

            Element.IpInfo -> {
                val state = viewModel.portForwardingStatus
                IPTile(
                    isPortForwardingEnabled = viewModel.isPortForwardingEnabled(),
                    publicIp = viewModel.clientIp.value,
                    vpnIp = viewModel.vpnIp.value,
                    portForwardingStatus = when (state.value) {
                        PortForwardingStatus.Error -> stringResource(id = com.kape.ui.R.string.pfwd_error)
                        PortForwardingStatus.NoPortForwarding -> stringResource(id = com.kape.ui.R.string.pfwd_disabled)
                        PortForwardingStatus.Requesting -> stringResource(id = com.kape.ui.R.string.pfwd_requesting)
                        PortForwardingStatus.Success -> viewModel.port.value.toString()
                        else -> ""
                    },
                )
            }

            Element.QuickConnect -> {
                QuickConnect(
                    servers = viewModel.quickConnectVpnServers.value,
                    onClick = {
                        viewModel.quickConnect(it)
                    },
                )
            }

            Element.QuickSettings -> {
                QuickSettings(
                    onKillSwitchClick = {
                        viewModel.navigateToKillSwitch()
                    },
                    onAutomationClick = {
                        viewModel.navigateToAutomation()
                    },
                    onProtocolsClick = {
                        viewModel.navigateToProtocols()
                    },
                )
            }

            Element.VpnRegionSelection -> {
                viewModel.selectedVpnServer.value?.let {
                    VpnLocationPicker(server = it, isConnected = viewModel.isConnectionActive()) {
                        viewModel.showVpnRegionSelection()
                    }
                }
            }

            Element.ShadowsocksRegionSelection -> {
                viewModel.getSelectedShadowsocksServer()?.let {
                    ShadowsocksLocationPicker(
                        server = it,
                        isConnected = viewModel.isConnectionActive(),
                    ) {
                        viewModel.showShadowsocksRegionSelection()
                    }
                }
            }

            Element.Snooze -> {
                Snooze(
                    active = viewModel.isSnoozeActive,
                    timeUntilResume = when (viewModel.timeUntilResume.intValue) {
                        1 -> String.format(
                            stringResource(id = com.kape.ui.R.string.minute_to_format),
                            viewModel.timeUntilResume.intValue,
                        )

                        else -> String.format(
                            stringResource(id = com.kape.ui.R.string.minutes_to_format),
                            viewModel.timeUntilResume.intValue,
                        )
                    },
                    onClick = {
                        if (viewModel.isConnectionActive()) {
                            if (!viewModel.isAlarmPermissionGranted()) {
                                context.startActivity(Intent(ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                            } else {
                                viewModel.snooze(it)
                            }
                        }
                    },
                    onResumeClick = {
                        viewModel.onSnoozeResumed()
                    },
                )
            }

            Element.Traffic -> {
                Traffic(
                    viewModel.download.value,
                    viewModel.upload.value,
                )
            }
        }
    }
}