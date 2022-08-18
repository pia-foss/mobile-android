package com.kape.connection.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.kape.connection.ui.vm.ConnectionViewModel
import com.kape.sidemenu.ui.SideMenuUiDrawer
import com.kape.uicomponents.components.AppBar
import com.kape.uicomponents.components.AppBarColors
import com.kape.uicomponents.components.AppBarState
import com.kape.uicomponents.components.Separator
import com.kape.uicomponents.theme.Space
import org.koin.androidx.compose.viewModel
import java.util.*

@Composable
fun ConnectionScreen() {

    val viewModel: ConnectionViewModel by viewModel()
    val state by remember(viewModel) { viewModel.state }.collectAsState()
    val locale = Locale.getDefault().language

    LaunchedEffect(key1 = Unit) {
        viewModel.loadServers(locale)
    }

    SideMenuUiDrawer {
        Column(modifier = Modifier.fillMaxSize()) {
            AppBar(
                onClick = { openDrawer() },
                state = AppBarState(
                    "",
                    AppBarColors.Default,
                    showLogo = true,
                    showMenu = true,
                    showOverflow = false
                )
            )
            Spacer(modifier = Modifier.height(Space.NORMAL))
            ConnectionButton(ConnectionState.Default)

            state.selectedServer?.let {
                RegionInformationTile(server = it)
            }
            Separator()
            // TODO: hardcoded data for display purposes, will be updated when VPN manager is integrated
            IpInformationTile(ip = "91.155.24.17", vpnIp = "---")
            Separator()
            QuickSettingsTile()
            Separator()
            SnoozeTile(state.snoozeState, viewModel)
        }
    }
}