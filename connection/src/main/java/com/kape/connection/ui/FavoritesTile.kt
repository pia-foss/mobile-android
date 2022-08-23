package com.kape.connection.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.TopEnd
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.kape.connection.R
import com.kape.region_selection.server.Server
import com.kape.region_selection.utils.getFlagResource
import com.kape.uicomponents.theme.*

const val MAX_FAVORITE_SERVERS = 6

@Composable
fun FavoritesTile(favoriteServers: List<Server>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Space.NORMAL)
    ) {
        Text(
            text = stringResource(id = R.string.favorite_servers),
            color = Grey55,
            fontSize = FontSize.Small
        )
        Spacer(modifier = Modifier.height(Space.SMALL))
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            if (favoriteServers.isEmpty()) {
                for (index in 1..MAX_FAVORITE_SERVERS) {
                    FavoriteServer(modifier = Modifier.weight(1f))
                }
            } else {
                for (index in 0 until MAX_FAVORITE_SERVERS) {
                    if (favoriteServers.getOrNull(index) != null) {
                        FavoriteServer(
                            server = favoriteServers[index],
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        FavoriteServer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun FavoriteServer(server: Server? = null, modifier: Modifier) {
    Column(modifier = modifier, horizontalAlignment = CenterHorizontally) {
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
                            .align(TopEnd)
                            .size(Square.DIP_BADGE)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(Space.MINI))
        Text(text = server?.iso ?: "", fontSize = FontSize.Tiny, color = Grey20)
    }
}