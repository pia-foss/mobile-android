package com.kape.connection.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.kape.appbar.view.ConnectionAppBar
import com.kape.appbar.viewmodel.AppBarViewModel
import com.kape.connection.ui.tiles.ConnectionInfoTile
import com.kape.connection.ui.tiles.FavoritesTile
import com.kape.connection.ui.tiles.IpInformationTile
import com.kape.connection.ui.tiles.QuickConnectTile
import com.kape.connection.ui.tiles.QuickSettingsTile
import com.kape.connection.ui.tiles.RegionInformationTile
import com.kape.connection.ui.tiles.SnoozeTile
import com.kape.connection.ui.tiles.UsageTile
import com.kape.connection.ui.vm.ConnectionViewModel
import com.kape.sidemenu.ui.SideMenuUiDrawer
import com.kape.ui.elements.Separator
import com.kape.ui.theme.Space
import com.kape.vpnconnect.utils.ConnectionManager
import com.kape.vpnconnect.utils.ConnectionStatus
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import java.util.Locale

@Composable
fun ConnectionScreen() {
    val viewModel: ConnectionViewModel = koinViewModel()
    val appBarViewModel: AppBarViewModel = koinViewModel()
    val state by remember(viewModel) { viewModel.state }.collectAsState()
    val locale = Locale.getDefault().language
    val connectionManager: ConnectionManager = koinInject()
    val connectionStatus = connectionManager.connectionStatus.collectAsState()
    val connectionState = when (connectionStatus.value) {
        ConnectionStatus.CONNECTED -> ConnectionState.Connected
        ConnectionStatus.CONNECTING -> ConnectionState.Connecting
        ConnectionStatus.DISCONNECTED -> ConnectionState.Default
        ConnectionStatus.RECONNECTING -> ConnectionState.Connecting
    }

    LaunchedEffect(key1 = Unit) {
        viewModel.loadServers(locale)
        viewModel.autoConnect()
    }

    SideMenuUiDrawer {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            ConnectionAppBar(
                viewModel = appBarViewModel,
                onHeaderClick = { /*TODO*/ },
                onLeftButtonClick = { openDrawer() },
            )
            Spacer(modifier = Modifier.height(Space.NORMAL))
            ConnectionButton(connectionState) {
                viewModel.onConnectionButtonClicked()
            }

            state.selectedServer?.let {
                RegionInformationTile(server = it) {
                    viewModel.showRegionSelection()
                }
            }
            Separator()
            IpInformationTile(
                ip = viewModel.ip,
                vpnIp = viewModel.vpnIp,
            )
            Separator()
            QuickSettingsTile()
            Separator()
            QuickConnectTile(servers = state.quickConnectServers)
            Separator()
            FavoritesTile(state.favoriteServers)
            Separator()
            SnoozeTile(state.snoozeState, viewModel)
            Separator()
            UsageTile(state.usageState)
            Separator()
            ConnectionInfoTile(viewModel.getConnectionSettings())
        }
    }
}