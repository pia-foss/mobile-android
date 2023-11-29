package com.kape.connection.ui.tiles

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kape.connection.R
import com.kape.ui.text.QuickConnectText
import com.kape.ui.text.TileTitleText
import com.kape.ui.utils.LocalColors
import com.kape.ui.utils.getFlagResource
import com.kape.utils.server.VpnServer

@Composable
fun QuickConnect(servers: List<VpnServer>, onClick: (serverKey: String) -> Unit, modifier: Modifier) {
    Column(
        modifier = modifier,
    ) {
        TileTitleText(content = stringResource(id = R.string.quick_connect))
        Spacer(modifier = Modifier.height(8.dp))
        Row {
            if (servers.isEmpty()) {
                for (index in 1..MAX_SERVERS) {
                    ServerTile(modifier = Modifier.weight(1f))
                }
            } else {
                for (index in 0 until MAX_SERVERS) {
                    if (servers.getOrNull(index) != null) {
                        ServerTile(
                            server = servers[index],
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    onClick(servers[index].key)
                                },
                        )
                    } else {
                        ServerTile(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickConnectItem(server: VpnServer?) {
    Column {
        Box {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(LocalColors.current.onPrimary, CircleShape)
                    .padding(4.dp),
            ) {
                Image(
                    painter = painterResource(
                        id = server?.let { getFlagResource(LocalContext.current, it.iso) }
                            ?: R.drawable.ic_map_empty,
                    ),
                    contentScale = ContentScale.Crop,
                    contentDescription = null,
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(36.dp)
                        .align(Alignment.Center),
                )
            }
            server?.let {
                if (it.isDedicatedIp) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_dip_badge),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(10.dp),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        QuickConnectText(
            content = server?.iso ?: "",
            modifier = Modifier.align(CenterHorizontally),
        )
    }
}