package com.kape.regionselection.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.kape.regionselection.R
import com.kape.regions.utils.REGIONS_PING_TIMEOUT
import com.kape.ui.theme.FontSize
import com.kape.ui.theme.Height
import com.kape.ui.theme.Space
import com.kape.ui.theme.Width
import com.kape.ui.theme.getLatencyColor
import com.kape.ui.utils.LocalColors
import com.kape.ui.utils.getFlagResource
import com.kape.utils.server.Server

@Composable
fun ServerListItem(
    server: Server,
    isFavorite: MutableState<Boolean>,
    onClick: ((server: Server) -> Unit),
    onFavoriteClick: ((serverName: String) -> Unit),
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(Height.DEFAULT)
            .clickable {
                onClick(server)
            }
            .padding(horizontal = Space.NORMAL),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(id = getFlagResource(LocalContext.current, server.iso)),
            contentDescription = stringResource(id = R.string.flag),
            modifier = Modifier
                .width(Width.FLAG)
                .height(Height.FLAG),
        )
        Text(
            text = server.name,
            fontSize = FontSize.Normal,
            modifier = Modifier.padding(horizontal = Space.SMALL),
        )

        if (server.isGeo) {
            Image(
                painter = painterResource(id = R.drawable.ic_geo_default),
                contentDescription = stringResource(id = R.string.geo),
                modifier = Modifier
                    .padding(end = Space.SMALL)
                    .width(Width.SERVER_ICON)
                    .height(Height.SERVER_ICON),
            )
        }

        if (server.isAllowsPF) {
            Image(
                painter = painterResource(id = R.drawable.ic_port_forwarding),
                contentDescription = stringResource(id = R.string.port_forwarding),
                modifier = Modifier
                    .padding(end = Space.SMALL)
                    .width(Width.SERVER_ICON)
                    .height(Height.SERVER_ICON),
            )
        }

        // TODO: if server is selected - add appropriate image && update respective colors

        if (server.isOffline) {
            Image(
                painter = painterResource(id = R.drawable.ic_offline),
                contentDescription = stringResource(id = R.string.offline),
                modifier = Modifier
                    .padding(end = Space.SMALL)
                    .width(Width.SERVER_ICON)
                    .height(Height.SERVER_ICON),
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = if (server.latency != null && server.latency!!.toInt() < REGIONS_PING_TIMEOUT) {
                stringResource(id = com.kape.ui.R.string.latency_to_format).format(server.latency)
            } else {
                ""
            },
            fontSize = FontSize.Small,
            color = LocalColors.current.getLatencyColor(server.latency),
            modifier = Modifier.padding(horizontal = Space.SMALL),
        )

        IconButton(onClick = {
            isFavorite.value = !isFavorite.value
            onFavoriteClick(server.name)
        },) {
            Image(
                painter = painterResource(id = if (isFavorite.value) R.drawable.ic_favorite_selected else R.drawable.ic_favorite_default),
                contentDescription = stringResource(id = R.string.favorite),
                modifier = Modifier
                    .width(Width.FAVOURITE)
                    .height(Height.FAVOURITE),
            )
        }
    }
}