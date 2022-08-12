package com.kape.connection.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.core.os.ConfigurationCompat
import com.kape.connection.ui.vm.ConnectionViewModel
import com.kape.sidemenu.ui.SideMenuUiDrawer
import com.kape.uicomponents.components.AppBar
import com.kape.uicomponents.components.AppBarColors
import com.kape.uicomponents.components.AppBarState
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

        }
    }
}