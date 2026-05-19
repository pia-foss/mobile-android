package com.kape.sharedui.tiles

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kape.data.QUICK_CONNECT_MAX_SERVERS
import com.kape.data.vpnserver.VpnServer
import com.kape.ui.R
import com.kape.ui.mobile.text.QuickConnectText
import com.kape.ui.mobile.text.TileTitleText
import com.kape.ui.theme.PiaTypography
import com.kape.ui.utils.LocalColors
import com.kape.ui.utils.getFlagResource

@Composable
fun QuickConnect(
    modifier: Modifier = Modifier,
    servers: Map<VpnServer?, Boolean>,
    isConnected: Boolean,
    onClick: (server: VpnServer) -> Unit,
) {
    val showInfoText = remember { mutableStateOf(false) }

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TileTitleText(content = stringResource(id = R.string.quick_connect))
            if (isConnected) {
                Spacer(Modifier.width(8.dp))
                IconButton(
                    modifier = Modifier.size(16.dp),
                    onClick = {
                        showInfoText.value = !showInfoText.value
                    },
                ) {
                    Icon(
                        painterResource(R.drawable.ic_info),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(16.dp),
                    )
                }
            } else {
                showInfoText.value = false
            }
        }
        if (showInfoText.value) {
            Text(
                text = stringResource(R.string.dialog_change_location_message),
                color = LocalColors.current.onSurface,
                style = PiaTypography.body3,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row {
            if (servers.isEmpty()) {
                for (index in 1..QUICK_CONNECT_MAX_SERVERS) {
                    QuickConnectItem(modifier = Modifier.weight(1f))
                }
            } else {
                for (index in 0 until QUICK_CONNECT_MAX_SERVERS) {
                    val server = servers.keys.toList().getOrNull(index)
                    server?.let { current ->
                        QuickConnectItem(
                            server = current,
                            isFavorite = servers[current] ?: false,
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .clickable {
                                        servers.keys.toList()[index]?.let {
                                            onClick(it)
                                        }
                                    }.testTag(":QuickConnect:server_$index"),
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
                modifier =
                    Modifier
                        .size(40.dp)
                        .background(LocalColors.current.surfaceVariant, CircleShape)
                        .padding(8.dp),
            ) {
                Image(
                    painter =
                        painterResource(
                            id =
                                server?.let { getFlagResource(LocalContext.current, it.iso) }
                                    ?: R.drawable.ic_map_empty,
                        ),
                    contentScale = ContentScale.Crop,
                    contentDescription = null,
                    modifier =
                        Modifier
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
                        modifier =
                            Modifier
                                .align(Alignment.BottomEnd)
                                .size(12.dp),
                    )
                }
                if (isFavorite) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_favorite),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier =
                            Modifier
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