package com.kape.ui.mobile.tiles

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.kape.ui.R
import com.kape.ui.mobile.text.QuickConnectText
import com.kape.ui.mobile.text.TileTitleText
import com.kape.ui.utils.LocalColors
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
            .padding(horizontal = 32.dp, vertical = 16.dp),
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
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    servers.keys.toList()[index]?.let {
                                        onClick(it.key)
                                    }
                                },
                        )
                    } ?: run {
                        QuickConnectItem(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickConnectItem(
    server: VpnServer? = null,
    isFavorite: Boolean = false,
    modifier: Modifier,
) {
    Column(modifier = modifier, horizontalAlignment = CenterHorizontally) {
        Box {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(LocalColors.current.surfaceVariant, CircleShape)
                    .padding(8.dp),
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
        Spacer(modifier = Modifier.height(4.dp))
        val serverText = server?.let { if (it.isDedicatedIp) "DIP - ${it.iso}" else it.iso } ?: ""
        QuickConnectText(
            content = serverText,
            modifier = Modifier.align(CenterHorizontally),
        )
    }
}