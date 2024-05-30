package com.kape.connection.ui.mobile

import android.content.Context
import android.content.Intent
import android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
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
import com.kape.connection.utils.ConnectionScreenState
import com.kape.customization.data.Element
import com.kape.customization.data.ScreenElement
import com.kape.portforwarding.data.model.PortForwardingStatus
import com.kape.sidemenu.ui.screens.mobile.SideMenuContent
import com.kape.ui.R
import com.kape.ui.mobile.elements.InfoCard
import com.kape.ui.mobile.elements.RoundIconButton
import com.kape.ui.mobile.elements.Screen
import com.kape.ui.mobile.elements.Separator
import com.kape.ui.mobile.elements.TertiaryButton
import com.kape.ui.mobile.text.DedicatedIpHomeBannerText
import com.kape.ui.mobile.tiles.ConnectionInfo
import com.kape.ui.mobile.tiles.IPTile
import com.kape.ui.mobile.tiles.QuickConnect
import com.kape.ui.mobile.tiles.QuickSettings
import com.kape.ui.mobile.tiles.ShadowsocksLocationPicker
import com.kape.ui.mobile.tiles.Snooze
import com.kape.ui.mobile.tiles.Traffic
import com.kape.ui.mobile.tiles.VpnLocationPicker
import com.kape.ui.utils.LocalColors
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
    val state = viewModel.state.collectAsState()

    BackHandler {
        viewModel.exitApp()
    }

    LaunchedEffect(key1 = Unit) {
        viewModel.loadVpnServers(locale)
        viewModel.loadShadowsocksServers(locale)
        viewModel.shouldShowDedicatedIpSignupBanner()
        viewModel.autoConnect()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                SideMenuContent(scope = scope, state = drawerState)
            }
        },
    ) {
        Scaffold(
            topBar = {
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
            },
        ) {
            Column(
                modifier = Modifier
                    .padding(it)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .semantics {
                        testTagsAsResourceId = true
                    },
                horizontalAlignment = CenterHorizontally,
            ) {
                Column(modifier = Modifier.widthIn(max = 520.dp)) {
                    var connectButtonDescription = stringResource(id = R.string.toggle_connection_button)

                    connectButtonDescription += when (connectionStatus.value) {
                        ConnectionStatus.CONNECTED, ConnectionStatus.CONNECTING, ConnectionStatus.RECONNECTING -> stringResource(id = R.string.disconnect_from_vpn)
                        else -> stringResource(id = R.string.connect_to_vpn)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    if (viewModel.showDedicatedIpHomeBanner.value) {
                        DedicatedIpBanner(
                            onAcceptClick = {
                                viewModel.navigateToDedicatedIpPlans()
                            },
                            onCancelClick = {
                                viewModel.hideDedicatedIpSignupBanner()
                            },
                        )
                    }
                    ConnectButton(
                        status = if (isConnected.value) connectionStatus.value else ConnectionStatus.ERROR,
                        onTvLayout = false,
                        modifier = Modifier
                            .align(CenterHorizontally)
                            .testTag(":ConnectionScreen:connection_button")
                            .semantics(mergeDescendants = true) {
                                role = Role.Button
                                contentDescription = connectButtonDescription
                            },
                    ) {
                        viewModel.onConnectionButtonClicked()
                    }
                    Spacer(modifier = Modifier.height(36.dp))
                    viewModel.getOrderedElements().forEach { screenElement ->
                        DisplayComponent(
                            screenElement = screenElement,
                            isVisible = viewModel.isScreenElementVisible(screenElement),
                            viewModel = viewModel,
                            state = state.value,
                        )
                    }
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
    state: ConnectionScreenState,
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
                for (server in state.quickConnectServers) {
                    quickConnectMap[server] =
                        viewModel.isVpnServerFavorite(server.name, server.isDedicatedIp)
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
                VpnLocationPicker(
                    modifier = Modifier.testTag(":ConnectionScreen:VpnLocationPicker"),
                    server = state.server,
                    isConnected = viewModel.isConnectionActive(),
                    isOptimal = state.isCurrentServerOptimal,
                ) {
                    viewModel.showVpnRegionSelection()
                }

                Spacer(modifier = Modifier.height(8.dp))
                if (state.showOptimalLocationInfo) {
                    InfoCard(
                        content = stringResource(id = R.string.optimal_location_hint),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                    )
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

@Composable
private fun DedicatedIpBanner(
    onAcceptClick: () -> Unit,
    onCancelClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .background(LocalColors.current.primary)
            .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DedicatedIpHomeBannerText(
            modifier = Modifier.weight(1.0f),
            content = stringResource(id = R.string.dip_signup_banner_home_description),
        )
        Spacer(modifier = Modifier.width(8.dp))
        TertiaryButton(
            text = stringResource(id = R.string.yes_i_want),
            onClick = onAcceptClick,
        )
        RoundIconButton(
            painterId = R.drawable.ic_close,
            onClick = onCancelClick,
        )
    }
    Spacer(modifier = Modifier.height(16.dp))
}