package com.kape.uicomponents.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.kape.uicomponents.R
import com.kape.uicomponents.theme.*
import com.kape.uicomponents.theme.Width.TOOLBAR_LOGO

@Composable
fun AppBar(onClick: () -> Unit, state: AppBarState) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .background(state.color.brush)) {
        IconButton(onClick = onClick, modifier = Modifier.padding(Space.SMALL)) {
            Icon(state.icon, contentDescription = "")
        }
        if (state.showLogo) {
            Image(painter = painterResource(id = R.drawable.ic_pia_logo),
                contentDescription = "",
                modifier = Modifier
                    .width(TOOLBAR_LOGO)
                    .align(CenterVertically))
        } else {
            Text(text = state.title, modifier = Modifier.align(CenterVertically), fontSize = FontSize.Title, color = state.color.textColor)
        }
    }
}

data class AppBarState(val title: String, val icon: ImageVector, val color: AppBarColors, val showLogo: Boolean)

sealed class AppBarColors(val brush: Brush, val textColor: Color) {
    object Default : AppBarColors(Brush.verticalGradient(listOf(Grey85, Grey85)), Color.Black)
    object Disconnected : AppBarColors(Brush.verticalGradient(DisconnectedGradient), Color.White)
    object Connecting : AppBarColors(Brush.verticalGradient(ConnectingGradient), Color.White)
    object Connected : AppBarColors(Brush.verticalGradient(ConnectedGradient), Color.White)
}

@Preview
@Composable
fun TestAppBar() {
    AppBar(onClick = {}, state = AppBarState("hello", Icons.Filled.Menu, AppBarColors.Default, true))
}
