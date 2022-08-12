package com.kape.connection.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.kape.connection.R
import com.kape.uicomponents.theme.*

@Composable
fun IpInformationTile(ip: String, vpnIp: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Space.NORMAL, vertical = Space.MEDIUM)
    ) {
        Column(
            modifier = Modifier
                .weight(0.5f)
                .align(CenterVertically)
        ) {
            Text(
                text = stringResource(id = R.string.public_ip),
                color = Grey55,
                fontSize = FontSize.Small
            )
            Spacer(modifier = Modifier.height(Space.MINI))
            Text(text = ip, color = Grey20, fontSize = FontSize.Normal)
        }
        Row(Modifier.weight(0.5f)) {
            Icon(
                painter = painterResource(id = R.drawable.ic_ip_triangle),
                contentDescription = stringResource(
                    id = com.kape.sidemenu.R.string.icon
                ),
                tint = Color.Unspecified,
                modifier = Modifier
                    .width(Width.IP_ARROW)
                    .height(Height.IP_ARROW)
                    .align(CenterVertically)
            )
            Spacer(modifier = Modifier.width(Space.MEDIUM))
            Column(modifier = Modifier.align(CenterVertically)) {
                Text(
                    text = stringResource(id = R.string.vpn_ip),
                    color = Grey55,
                    fontSize = FontSize.Small
                )
                Spacer(modifier = Modifier.height(Space.MINI))
                Text(text = vpnIp, color = Grey20, fontSize = FontSize.Normal)
            }
        }
    }
}