package com.kape.connection.ui

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
import com.kape.connection.ui.tiles.LocationPicker
import com.kape.connection.ui.tiles.Snooze
import com.kape.connection.ui.vm.ConnectionViewModel
import com.kape.connection.utils.SnoozeInterval
import com.kape.portforwarding.data.model.PortForwardingStatus
import com.kape.sidemenu.ui.SideMenu
import com.kape.ui.elements.Screen
import com.kape.ui.elements.Separator
import com.kape.ui.tiles.ConnectionInfo
import com.kape.ui.tiles.IPTile
import com.kape.ui.tiles.QuickConnect
import com.kape.ui.tiles.QuickSettings
import com.kape.ui.tiles.Traffic
import com.kape.vpnconnect.utils.ConnectionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import java.util.Locale

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ConnectionScreen() = Screen {
    val viewModel: ConnectionViewModel = koinViewModel()
    val appBarViewModel: AppBarViewModel = koinViewModel()
    val locale = Locale.getDefault().language
    val context = LocalContext.current
    val connectionManager: ConnectionManager = koinInject()
    val connectionStatus = connectionManager.connectionStatus.collectAsState()
    val scope: CoroutineScope = rememberCoroutineScope()
    val drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed)

    BackHandler {
        viewModel.exitApp()
    }

    LaunchedEffect(key1 = Unit) {
        viewModel.loadVpnServers(locale)
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
                connectionStatus.value,
                Modifier
                    .align(CenterHorizontally)
                    .testTag(":ConnectionScreen:connection_button"),
            ) {
                viewModel.onConnectionButtonClicked()
            }
            viewModel.selectedVpnServer.value?.let {
                LocationPicker(server = it, isConnected = viewModel.isConnectionActive()) {
                    viewModel.showRegionSelection()
                }
            }
            val state = viewModel.portForwardingStatus.collectAsState()
            IPTile(
                isPortForwardingEnabled = viewModel.isPortForwardingEnabled(),
                publicIp = viewModel.ip,
                vpnIp = viewModel.vpnIp,
                portForwardingStatus = when (state.value) {
                    PortForwardingStatus.Error -> stringResource(id = com.kape.ui.R.string.pfwd_error)
                    PortForwardingStatus.NoPortForwarding -> stringResource(id = com.kape.ui.R.string.pfwd_disabled)
                    PortForwardingStatus.Requesting -> stringResource(id = com.kape.ui.R.string.pfwd_requesting)
                    PortForwardingStatus.Success -> viewModel.port.value.toString()
                    else -> ""
                },
            )
            Separator()
            QuickConnect(
                servers = viewModel.quickConnectVpnServers.value,
                onClick = {
                    viewModel.quickConnect(it)
                },
            )
            Separator()
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
            Separator()
            if (viewModel.snoozeTime.longValue != 0L && viewModel.snoozeTime.longValue < System.currentTimeMillis()) {
                viewModel.snooze(context, SnoozeInterval.SNOOZE_DEFAULT_MS)
            }
            Snooze(
                viewModel.snoozeActive,
                onClick = {
                    if (viewModel.isConnectionActive()) {
                        viewModel.snooze(context, it)
                    }
                },
                onResumeClick = {
                    viewModel.snooze(context, SnoozeInterval.SNOOZE_DEFAULT_MS)
                },
            )
            Separator()
            Traffic(
                viewModel.download.value,
                viewModel.upload.value,
            )
            Separator()
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
    }
}