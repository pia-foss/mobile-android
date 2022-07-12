package com.kape.connection.ui

import androidx.compose.runtime.Composable
import com.kape.sidemenu.ui.SideMenuUiDrawer
import com.kape.uicomponents.components.AppBar
import com.kape.uicomponents.components.AppBarColors
import com.kape.uicomponents.components.AppBarState

@Composable
fun ConnectionScreen() {
    SideMenuUiDrawer {
        AppBar(onClick = { openDrawer() },
            state = AppBarState("", AppBarColors.Default, showLogo = true, showMenu = true, showOverflow = false))
    }
}