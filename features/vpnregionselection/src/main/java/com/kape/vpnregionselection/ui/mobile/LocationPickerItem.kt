package com.kape.vpnregionselection.ui.mobile

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
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kape.ui.R
import com.kape.ui.mobile.elements.FavoriteIcon
import com.kape.ui.mobile.elements.Separator
import com.kape.ui.mobile.text.RegionSelectionDipText
import com.kape.ui.mobile.text.RegionSelectionIpText
import com.kape.ui.mobile.text.RegionSelectionLatencyText
import com.kape.ui.mobile.text.RegionSelectionText
import com.kape.ui.theme.getLatencyColor
import com.kape.ui.utils.LocalColors
import com.kape.ui.utils.getFlagResource
import com.kape.utils.vpnserver.VpnServer
import com.kape.vpnregions.utils.VPN_REGIONS_PING_TIMEOUT

@Composable
fun LocationPickerItem(
    server: VpnServer,
    isFavorite: Boolean,
    enableFavorite: Boolean,
    isPortForwardingEnabled: Boolean,
    onClick: ((server: VpnServer) -> Unit),
    onFavoriteVpnClick: ((vpnServerName: String) -> Unit),
) {
    Column {
        Row(
            modifier = Modifier
                .clickable {
                    onClick(server)
                }
                .alpha(if (!server.allowsPortForwarding && isPortForwardingEnabled) 0.5f else 1f)
                .defaultMinSize(minHeight = 56.dp)
                .padding(16.dp),
        ) {
            Icon(
                painter = painterResource(
                    id = getFlagResource(LocalContext.current, server.iso),
                ),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier
                    .size(24.dp),
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.align(CenterVertically).weight(12f)) {
                Row {
                    RegionSelectionText(content = server.name)
                    if (!server.allowsPortForwarding && isPortForwardingEnabled) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            painter = painterResource(id = com.kape.ui.R.drawable.ic_port_forwarding_unavailable),
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }

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
                if (it.toInt() < VPN_REGIONS_PING_TIMEOUT) {
                    Box(
                        modifier = Modifier
                            .align(CenterVertically)
                            .size(8.dp)
                            .background(
                                LocalColors.current.getLatencyColor(server.latency),
                                shape = CircleShape,
                            ),
                    )
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