package com.kape.connection.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.kape.appbar.view.ConnectionAppBar
import com.kape.appbar.viewmodel.AppBarViewModel
import com.kape.connection.ui.tiles.*
import com.kape.connection.ui.vm.ConnectionViewModel
import com.kape.sidemenu.ui.SideMenuUiDrawer
import com.kape.ui.elements.Separator
import com.kape.ui.theme.Space
import org.koin.androidx.compose.koinViewModel
import java.util.*

@Composable
fun ConnectionScreen() {

    val viewModel: ConnectionViewModel = koinViewModel()
    val appBarViewModel: AppBarViewModel = koinViewModel()
    val state by remember(viewModel) { viewModel.state }.collectAsState()
    val locale = Locale.getDefault().language

    LaunchedEffect(key1 = Unit) {
        viewModel.loadServers(locale)
    }

    SideMenuUiDrawer {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            ConnectionAppBar(
                viewModel = appBarViewModel,
                onHeaderClick = { /*TODO*/ },
                onLeftButtonClick = { openDrawer() })
            Spacer(modifier = Modifier.height(Space.NORMAL))
            ConnectionButton(ConnectionState.Default, {})

            state.selectedServer?.let {
                RegionInformationTile(server = it) {
                    viewModel.showRegionSelection()
                }
            }
            Separator()
            // TODO: hardcoded data for display purposes, will be updated when VPN manager is integrated
            IpInformationTile(ip = "91.155.24.17", vpnIp = "---")
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
            ConnectionInfoTile()
        }
    }
}