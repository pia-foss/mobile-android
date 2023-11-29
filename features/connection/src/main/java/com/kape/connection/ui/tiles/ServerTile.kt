package com.kape.connection.ui.tiles

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.kape.connection.R
import com.kape.ui.theme.FontSize
import com.kape.ui.theme.Height
import com.kape.ui.theme.Space
import com.kape.ui.theme.Square
import com.kape.ui.theme.Width
import com.kape.ui.utils.LocalColors
import com.kape.ui.utils.getFlagResource
import com.kape.utils.server.VpnServer

@Composable
fun ServerTile(server: VpnServer? = null, modifier: Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box {
            Icon(
                painter = painterResource(
                    id = server?.let { getFlagResource(LocalContext.current, it.iso) }
                        ?: R.drawable.ic_map_empty,
                ),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier
                    .padding(top = Space.MINI, end = Space.MINI)
                    .width(Width.FLAG)
                    .height(Height.FLAG),
            )
            server?.let {
                if (it.isDedicatedIp) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_dip_badge),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(Square.DIP_BADGE),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(Space.MINI))
        Text(
            text = server?.iso ?: "",
            fontSize = FontSize.Tiny,
            color = LocalColors.current.onSurface,
        )
    }
}