package com.kape.ui.tiles

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kape.ui.R
import com.kape.ui.text.IPText
import com.kape.ui.text.TileTitleText
import com.kape.ui.utils.LocalColors

@Composable
fun IPTile(
    modifier: Modifier = Modifier,
    publicIp: String,
    vpnIp: String,
    isPortForwardingEnabled: Boolean,
    portForwardingStatus: String,
) {
    Row(modifier = modifier.padding(horizontal = 32.dp, vertical = 16.dp)) {
        Column(Modifier.weight(0.4f)) {
            TileTitleText(content = stringResource(id = R.string.public_ip).uppercase())
            Spacer(modifier = Modifier.height(4.dp))
            IPText(content = publicIp)
        }

        Icon(
            painter = painterResource(id = R.drawable.ic_arrow),
            contentDescription = null,
            tint = LocalColors.current.onSurface,
            modifier = Modifier
                .align(CenterVertically)
                .weight(0.2f),
        )

        Column(Modifier.weight(0.4f)) {
            TileTitleText(content = stringResource(id = R.string.vpn_ip).uppercase())
            Spacer(modifier = Modifier.height(4.dp))
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
                        text = portForwardingStatus,
                        color = LocalColors.current.onSurface,
                        fontSize = 14.sp,
                    )
                }
            }
        }
    }
}