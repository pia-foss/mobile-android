package com.kape.connection.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kape.sidemenu.ui.SideMenuUiDrawer
import com.kape.uicomponents.components.AppBar
import com.kape.uicomponents.components.AppBarColors
import com.kape.uicomponents.components.AppBarState
import com.kape.uicomponents.theme.Space

@Composable
fun ConnectionScreen() {
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
        }
    }
}