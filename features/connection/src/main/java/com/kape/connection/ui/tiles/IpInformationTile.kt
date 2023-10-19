package com.kape.connection.ui.tiles

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kape.connection.R
import com.kape.portforwarding.data.model.PortForwardingStatus
import com.kape.ui.theme.FontSize
import com.kape.ui.theme.Height
import com.kape.ui.theme.Space
import com.kape.ui.theme.Width
import com.kape.ui.utils.LocalColors

@Deprecated("to be removed")
@Composable
fun IpInformationTile(
    ip: String,
    vpnIp: String,
    isPortForwardingEnabled: Boolean,
    portForwardingStatus: MutableState<PortForwardingStatus>,
    port: String? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Space.NORMAL, vertical = Space.MEDIUM),
    ) {
        Column(
            modifier = Modifier
                .weight(0.5f)
                .align(CenterVertically),
        ) {
            Text(
                text = stringResource(id = R.string.public_ip),
                color = LocalColors.current.outlineVariant,
                fontSize = FontSize.Small,
            )
            Spacer(modifier = Modifier.height(Space.MINI))
            Text(text = ip, color = LocalColors.current.onSurface, fontSize = FontSize.Normal)
        }
        Row(Modifier.weight(0.5f)) {
            Icon(
                painter = painterResource(id = R.drawable.ic_ip_triangle),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier
                    .width(Width.IP_ARROW)
                    .height(Height.IP_ARROW)
                    .align(CenterVertically),
            )
            Spacer(modifier = Modifier.width(Space.MEDIUM))
            Column(modifier = Modifier.align(CenterVertically)) {
                Text(
                    text = stringResource(id = R.string.vpn_ip),
                    color = LocalColors.current.outlineVariant,
                    fontSize = FontSize.Small,
                )
                Spacer(modifier = Modifier.height(Space.MINI))
                Text(
                    text = vpnIp,
                    color = LocalColors.current.onSurface,
                    fontSize = FontSize.Normal,
                )
                if (isPortForwardingEnabled) {
                    Row {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_port_forwarding),
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier
                                .size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (portForwardingStatus.value) {
                                PortForwardingStatus.Error -> "Error"
                                PortForwardingStatus.NoPortForwarding -> "---"
                                PortForwardingStatus.Requesting -> "Requesting"
                                PortForwardingStatus.Success -> port ?: ""
                            },
                            color = LocalColors.current.onSurface,
                            fontSize = FontSize.Normal,
                        )
                    }
                }
            }
        }
    }
}