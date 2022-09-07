package com.kape.connection.ui.tiles

import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.kape.connection.R
import com.kape.core.server.Server
import com.kape.uicomponents.theme.*
import com.kape.uicomponents.utils.getFlagResource

@Composable
fun ServerTile(server: Server? = null, modifier: Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box {
            Icon(
                painter = painterResource(
                    id = server?.let { getFlagResource(LocalContext.current, it.iso) }
                        ?: R.drawable.ic_map_empty
                ),
                contentDescription = stringResource(
                    id = com.kape.sidemenu.R.string.icon
                ), tint = Color.Unspecified,
                modifier = Modifier
                    .padding(top = Space.MINI, end = Space.MINI)
                    .width(Width.FLAG)
                    .height(Height.FLAG)
            )
            server?.let {
                if (it.isDedicatedIp()) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_dip_badge),
                        contentDescription = stringResource(
                            id = com.kape.sidemenu.R.string.icon
                        ),
                        tint = Color.Unspecified, modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(Square.DIP_BADGE)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(Space.MINI))
        Text(text = server?.iso ?: "", fontSize = FontSize.Tiny, color = Grey20)
    }
}