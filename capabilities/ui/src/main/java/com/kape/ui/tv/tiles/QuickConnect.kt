package com.kape.ui.tv.tiles

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import com.kape.ui.R
import com.kape.ui.tv.elements.TileButton
import com.kape.ui.tv.text.QuickConnectText
import com.kape.ui.tv.text.TileTitleText
import com.kape.ui.utils.getFlagResource
import com.kape.utils.vpnserver.VpnServer

const val MAX_SERVERS = 5

@Composable
fun QuickConnect(
    modifier: Modifier = Modifier,
    servers: Map<VpnServer?, Boolean>,
    onClick: (serverKey: String) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        TileTitleText(content = stringResource(id = R.string.quick_connect))
        Spacer(modifier = Modifier.height(8.dp))
        Row {
            if (servers.isEmpty()) {
                for (index in 1..MAX_SERVERS) {
                    QuickConnectItem(modifier = Modifier.weight(1f))
                }
            } else {
                for (index in 0 until MAX_SERVERS) {
                    val server = servers.keys.toList().getOrNull(index)
                    server?.let { current ->
                        QuickConnectItem(
                            server = current,
                            isFavorite = servers[current] ?: false,
                            onClick = {
                                servers.keys.toList()[index]?.let {
                                    onClick(it.key)
                                }
                            },
                            modifier = Modifier.weight(1f),
                        )
                    } ?: run {
                        QuickConnectItem(
                            modifier = Modifier
                                .weight(1f)
                                .focusProperties { canFocus = false },
                        )
                    }
                    if (index < MAX_SERVERS - 1) {
                        Spacer(modifier = Modifier.width(16.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun QuickConnectItem(
    server: VpnServer? = null,
    isFavorite: Boolean = false,
    onClick: () -> Unit = { },
    modifier: Modifier,
) {
    TileButton(
        modifier = modifier,
        onClick = onClick,
    ) {
        Box {
            Column(
                modifier = Modifier.padding(8.dp),
                horizontalAlignment = CenterHorizontally,
            ) {
                Image(
                    painter = painterResource(
                        id = server?.let { getFlagResource(LocalContext.current, it.iso) }
                            ?: R.drawable.ic_map_empty,
                    ),
                    contentScale = ContentScale.Crop,
                    contentDescription = null,
                    alignment = Alignment.Center,
                    modifier = Modifier
                        .requiredSize(40.dp)
                        .clip(CircleShape),
                )
                Spacer(modifier = Modifier.height(8.dp))
                val serverText =
                    server?.let { if (it.isDedicatedIp) "DIP - ${it.iso}" else it.iso } ?: ""
                QuickConnectText(
                    modifier = Modifier,
                    content = serverText,
                )
            }
            server?.let {
                if (it.isDedicatedIp) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_dip_badge),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(12.dp),
                    )
                }
                if (isFavorite) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_favorite),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(12.dp),
                    )
                }
            }
        }
    }
}