package com.kape.connection.ui.mobile

import android.content.Context
import android.content.Intent
import android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.dp
import com.kape.appbar.view.mobile.AppBar
import com.kape.appbar.view.mobile.AppBarType
import com.kape.appbar.viewmodel.AppBarViewModel
import com.kape.connection.ui.ConnectButton
import com.kape.connection.ui.vm.ConnectionViewModel
import com.kape.customization.data.Element
import com.kape.customization.data.ScreenElement
import com.kape.portforwarding.data.model.PortForwardingStatus
import com.kape.sidemenu.ui.SideMenu
import com.kape.ui.R
import com.kape.ui.mobile.elements.Screen
import com.kape.ui.mobile.elements.Separator
import com.kape.ui.mobile.tiles.ConnectionInfo
import com.kape.ui.mobile.tiles.IPTile
import com.kape.ui.mobile.tiles.QuickConnect
import com.kape.ui.mobile.tiles.QuickSettings
import com.kape.ui.mobile.tiles.ShadowsocksLocationPicker
import com.kape.ui.mobile.tiles.Snooze
import com.kape.ui.mobile.tiles.Traffic
import com.kape.ui.mobile.tiles.VpnLocationPicker
import com.kape.utils.vpnserver.VpnServer
import com.kape.vpnconnect.utils.ConnectionManager
import com.kape.vpnconnect.utils.ConnectionStatus
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
    val connectionManager: ConnectionManager = koinInject()
    val connectionStatus = connectionManager.connectionStatus.collectAsState()
    val isConnected = viewModel.isConnected.collectAsState()
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
            horizontalAlignment = CenterHorizontally,
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
            Column(modifier = Modifier.widthIn(max = 520.dp)) {
                val connection = stringResource(id = R.string.connection)
                Spacer(modifier = Modifier.height(16.dp))
                ConnectButton(
                    if (isConnected.value) connectionStatus.value else ConnectionStatus.ERROR,
                    Modifier
                        .align(CenterHorizontally)
                        .testTag(":ConnectionScreen:connection_button")
                        .semantics(mergeDescendants = true) {
                            role = Role.Button
                            contentDescription = connection
                        },
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
                }
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
                        PortForwardingStatus.Error -> stringResource(id = R.string.pfwd_error)
                        PortForwardingStatus.NoPortForwarding -> stringResource(id = R.string.pfwd_disabled)
                        PortForwardingStatus.Requesting -> stringResource(id = R.string.pfwd_requesting)
                        PortForwardingStatus.Success -> viewModel.port.value.toString()
                        else -> ""
                    },
                )
                Separator()
            }

            Element.QuickConnect -> {
                val quickConnectMap = mutableMapOf<VpnServer?, Boolean>()
                for (server in viewModel.quickConnectVpnServers.value) {
                    quickConnectMap[server] = viewModel.isVpnServerFavorite(server.name)
                }
                QuickConnect(
                    servers = quickConnectMap,
                    onClick = {
                        viewModel.quickConnect(it)
                    },
                )
                Separator()
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
                Separator()
            }

            Element.VpnRegionSelection -> {
                viewModel.selectedVpnServer.value?.let {
                    VpnLocationPicker(
                        server = it,
                        isConnected = viewModel.isConnectionActive(),
                        isOptimal = viewModel.showOptimalLocation.value,
                    ) {
                        viewModel.showVpnRegionSelection()
                    }
                }
            }

            Element.ShadowsocksRegionSelection -> {
                ShadowsocksLocationPicker(
                    server = viewModel.getSelectedShadowsocksServer(),
                    isConnected = viewModel.isConnectionActive(),
                ) {
                    viewModel.showShadowsocksRegionSelection()
                }
            }

            Element.Snooze -> {
                Snooze(
                    active = viewModel.isSnoozeActive,
                    timeUntilResume = when (viewModel.timeUntilResume.intValue) {
                        1 -> String.format(
                            stringResource(id = R.string.minute_to_format),
                            viewModel.timeUntilResume.intValue,
                        )

                        else -> String.format(
                            stringResource(id = R.string.minutes_to_format),
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
                Separator()
            }

            Element.Traffic -> {
                Traffic(
                    download = viewModel.download.value,
                    upload = viewModel.upload.value,
                )
                Separator()
            }
        }
    }
}