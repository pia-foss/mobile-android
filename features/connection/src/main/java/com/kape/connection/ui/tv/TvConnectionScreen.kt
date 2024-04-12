package com.kape.connection.ui.tv

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kape.appbar.view.tv.TvHomeHeaderItem
import com.kape.connection.ui.ConnectButton
import com.kape.connection.ui.vm.ConnectionViewModel
import com.kape.customization.data.Element
import com.kape.ui.mobile.elements.Screen
import com.kape.ui.mobile.tiles.ShadowsocksLocationPicker
import com.kape.ui.theme.statusBarConnected
import com.kape.ui.theme.statusBarConnecting
import com.kape.ui.theme.statusBarDefault
import com.kape.ui.theme.statusBarError
import com.kape.ui.tv.tiles.QuickConnect
import com.kape.ui.tv.tiles.VpnLocationPicker
import com.kape.ui.utils.LocalColors
import com.kape.utils.vpnserver.VpnServer
import com.kape.vpnconnect.utils.ConnectionManager
import com.kape.vpnconnect.utils.ConnectionStatus
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import java.util.Locale

@Composable
fun TvConnectionScreen() = Screen {
    val viewModel: ConnectionViewModel = koinViewModel()
    val connectionManager: ConnectionManager = koinInject()
    val connectionStatus = connectionManager.connectionStatus.collectAsState()
    val isConnected = viewModel.isConnected.collectAsState()
    val initialFocusRequester = remember { FocusRequester() }
    val locale = Locale.getDefault().language

    BackHandler {
        viewModel.exitApp()
    }

    LaunchedEffect(key1 = Unit) {
        initialFocusRequester.requestFocus()
        viewModel.loadVpnServers(locale)
        viewModel.loadShadowsocksServers(locale)
        viewModel.autoConnect()
    }

    Box(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .fillMaxSize(),
    ) {
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 4.dp,
            color = getTopBarConnectionColor(
                status = connectionStatus.value,
                scheme = LocalColors.current,
            ),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 24.dp)
                .background(LocalColors.current.background),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            TvHomeHeaderItem(
                modifier = Modifier.focusRequester(initialFocusRequester),
                connectionStatus = connectionStatus,
                defaultSelectedTabIndex = 0,
                onLocationsSelected = {
                    viewModel.showVpnRegionSelection()
                },
                onSettingsSelected = {
                    viewModel.navigateToSideMenu()
                },
                onHelpSelected = {
                    viewModel.navigateToHelp()
                },
            )
            Column(
                modifier = Modifier
                    .widthIn(max = 520.dp)
                    .fillMaxHeight(),
            ) {
                Spacer(modifier = Modifier.height(32.dp))
                ConnectButton(
                    status = if (isConnected.value) connectionStatus.value else ConnectionStatus.ERROR,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally),
                ) {
                    viewModel.onConnectionButtonClicked()
                }
                Spacer(modifier = Modifier.height(32.dp))
                viewModel.getOrderedElements().forEach {
                    DisplayComponent(
                        screenElement = it.element,
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
    screenElement: Element,
    isVisible: Boolean,
    viewModel: ConnectionViewModel,
) {
    if (isVisible.not()) {
        return
    }

    val state = viewModel.state.collectAsState()

    when (screenElement) {
        Element.VpnRegionSelection -> {
            VpnLocationPicker(
                server = state.value.server,
                isConnected = viewModel.isConnectionActive(),
                isOptimal = state.value.isCurrentServerOptimal,
            ) {
                viewModel.showVpnRegionSelection()
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

        Element.QuickConnect -> {
            val quickConnectMap = mutableMapOf<VpnServer?, Boolean>()
            for (server in state.value.quickConnectServers) {
                quickConnectMap[server] = viewModel.isVpnServerFavorite(server.name, server.isDedicatedIp)
            }
            QuickConnect(
                servers = quickConnectMap,
                onClick = {
                    viewModel.quickConnect(it)
                },
            )
        }

        Element.ConnectionInfo,
        Element.IpInfo,
        Element.QuickSettings,
        Element.Snooze,
        Element.Traffic,
        -> {
            // Continue. Not showing them on TV.
        }
    }
}

@Composable
private fun getTopBarConnectionColor(status: ConnectionStatus, scheme: ColorScheme): Color {
    return when (status) {
        ConnectionStatus.ERROR -> scheme.statusBarError()
        ConnectionStatus.CONNECTED -> scheme.statusBarConnected()
        ConnectionStatus.DISCONNECTED, ConnectionStatus.DISCONNECTING -> scheme.statusBarDefault(scheme)
        ConnectionStatus.RECONNECTING, ConnectionStatus.CONNECTING -> scheme.statusBarConnecting()
    }
}