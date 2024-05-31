package com.kape.ui.tv.tiles

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
    startQuickConnectFocusRequester: FocusRequester,
    servers: Map<VpnServer?, Boolean>,
    onClick: (server: VpnServer) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        TileTitleText(content = stringResource(id = R.string.quick_connect))
        Spacer(modifier = Modifier.height(8.dp))
        Row {
            for (index in 0 until MAX_SERVERS) {
                val itemModifier = if (index == 0) {
                    Modifier.focusRequester(startQuickConnectFocusRequester)
                } else {
                    Modifier
                }
                val server = servers.keys.toList().getOrNull(index)
                server?.let { current ->
                    QuickConnectItem(
                        modifier = itemModifier.weight(1f),
                        server = current,
                        isFavorite = servers[current] ?: false,
                        onClick = {
                            servers.keys.toList()[index]?.let {
                                onClick(it)
                            }
                        },
                    )
                } ?: run {
                    QuickConnectItem(
                        modifier = itemModifier
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
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxSize(),
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
                Row(
                    modifier = Modifier.fillMaxWidth().height(16.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    server?.let {
                        if (it.isDedicatedIp) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_dip_badge),
                                contentDescription = null,
                                tint = Color.Unspecified,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                        if (isFavorite) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_heart_selected),
                                contentDescription = null,
                                tint = Color.Unspecified,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}