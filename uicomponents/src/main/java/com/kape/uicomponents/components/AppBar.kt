package com.kape.uicomponents.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.kape.uicomponents.R
import com.kape.uicomponents.theme.*
import com.kape.uicomponents.theme.Width.TOOLBAR_LOGO

@Composable
fun AppBar(onClick: () -> Unit, state: AppBarState, onOverflowClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(Height.APP_BAR)
            .background(state.color.brush)
    ) {

        IconButton(onClick = onClick, modifier = Modifier.padding(Space.SMALL)) {
            if (state.showMenu) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_menu),
                    contentDescription = stringResource(id = R.string.menu)
                )
            } else {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = stringResource(id = R.string.back)
                )
            }
        }

        if (state.showLogo) {
            Image(
                painter = painterResource(id = R.drawable.ic_pia_logo),
                contentDescription = stringResource(id = R.string.logo),
                modifier = Modifier
                    .width(TOOLBAR_LOGO)
                    .align(CenterVertically)
            )
        } else {
            Text(
                text = state.title,
                modifier = Modifier.align(CenterVertically),
                fontSize = FontSize.Title,
                color = state.color.textColor
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        if (state.showOverflow) {
            IconButton(onClick = onOverflowClick!!, modifier = Modifier.align(CenterVertically)) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_sort),
                    contentDescription = stringResource(id = R.string.sort),
                )
            }
        }
    }
}

data class AppBarState(
    val title: String,
    val color: AppBarColors,
    val showLogo: Boolean,
    val showMenu: Boolean,
    val showOverflow: Boolean
)

sealed class AppBarColors(val brush: Brush, val textColor: Color) {
    object Default : AppBarColors(Brush.verticalGradient(listOf(Grey92, Grey92)), Color.Black)
    object Disconnected : AppBarColors(Brush.verticalGradient(DisconnectedGradient), Color.White)
    object Connecting : AppBarColors(Brush.verticalGradient(ConnectingGradient), Color.White)
    object Connected : AppBarColors(Brush.verticalGradient(ConnectedGradient), Color.White)
}