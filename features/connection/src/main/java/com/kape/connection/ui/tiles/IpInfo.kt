package com.kape.connection.ui.tiles

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.unit.sp
import com.kape.connection.R
import com.kape.portforwarding.data.model.PortForwardingStatus
import com.kape.ui.text.IPText
import com.kape.ui.text.TileTitleText
import com.kape.ui.utils.LocalColors

@Composable
fun IPTile(
    publicIp: String,
    vpnIp: String,
    isPortForwardingEnabled: Boolean,
    portForwardingStatus: MutableState<PortForwardingStatus>,
    port: String? = null,
) {
    Row(modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp)) {
        Column(Modifier.weight(0.4f)) {
            TileTitleText(content = stringResource(id = R.string.public_ip).uppercase())
            IPText(content = publicIp)
        }

        Icon(
            painter = painterResource(id = com.kape.ui.R.drawable.ic_arrow),
            contentDescription = null,
            tint = LocalColors.current.onSurface,
            modifier = Modifier
                .align(CenterVertically)
                .weight(0.2f),
        )

        Column(Modifier.weight(0.4f)) {
            TileTitleText(content = stringResource(id = R.string.vpn_ip))
            IPText(content = vpnIp)
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
                            PortForwardingStatus.Error -> stringResource(id = R.string.pfwd_error)
                            PortForwardingStatus.NoPortForwarding -> stringResource(id = R.string.pfwd_disabled)
                            PortForwardingStatus.Requesting -> stringResource(id = R.string.pfwd_requesting)
                            PortForwardingStatus.Success -> port ?: ""
                        },
                        color = LocalColors.current.onSurface,
                        fontSize = 14.sp,
                    )
                }
            }
        }
    }
}