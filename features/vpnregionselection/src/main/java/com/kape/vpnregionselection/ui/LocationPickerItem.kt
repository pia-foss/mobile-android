package com.kape.vpnregionselection.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kape.ui.elements.FavoriteIcon
import com.kape.ui.elements.Separator
import com.kape.ui.text.RegionSelectionDipText
import com.kape.ui.text.RegionSelectionIpText
import com.kape.ui.text.RegionSelectionLatencyText
import com.kape.ui.text.RegionSelectionText
import com.kape.ui.theme.getLatencyColor
import com.kape.ui.utils.LocalColors
import com.kape.ui.utils.getFlagResource
import com.kape.utils.vpnserver.VpnServer
import com.kape.vpnregions.utils.VPN_REGIONS_PING_TIMEOUT
import com.kape.vpnregionselection.R

@Composable
fun LocationPickerItem(
    server: VpnServer,
    isFavorite: Boolean,
    enableFavorite: Boolean,
    onClick: ((server: VpnServer) -> Unit),
    onFavoriteVpnClick: ((vpnServerName: String) -> Unit),
) {
    Column {
        Row(
            modifier = Modifier
                .clickable {
                    onClick(server)
                }
                .defaultMinSize(minHeight = 56.dp)
                .padding(16.dp),
        ) {
            Image(
                painter = painterResource(
                    id = getFlagResource(LocalContext.current, server.iso),
                ),
                contentScale = ContentScale.Crop,
                contentDescription = null,
                modifier = Modifier
                    .clip(CircleShape)
                    .size(24.dp),
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.align(CenterVertically)) {
                RegionSelectionText(content = server.name)
                server.dedicatedIp?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = CenterVertically) {
                        RegionSelectionIpText(content = it)
                        Spacer(modifier = Modifier.width(8.dp))
                        RegionSelectionDipText(content = stringResource(id = R.string.dedicated_ip))
                    }
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            server.latency?.let {
                Box(
                    modifier = Modifier
                        .align(CenterVertically)
                        .size(8.dp)
                        .background(
                            LocalColors.current.getLatencyColor(server.latency),
                            shape = CircleShape,
                        ),
                )
                if (it.toInt() < VPN_REGIONS_PING_TIMEOUT) {
                    Spacer(modifier = Modifier.width(8.dp))
                    RegionSelectionLatencyText(
                        "${server.latency}ms",
                        Modifier.align(CenterVertically),
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            if (enableFavorite) {
                FavoriteIcon(
                    isChecked = isFavorite,
                    modifier = Modifier
                        .align(CenterVertically)
                        .clickable {
                            onFavoriteVpnClick(server.name)
                        },
                )
            }
        }
        Separator()
    }
}