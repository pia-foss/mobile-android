package com.kape.sharedui.tiles

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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kape.contracts.ConnectionInfoProvider
import com.kape.data.portforwarding.PortForwardingStatus
import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.ui.R
import com.kape.ui.mobile.text.IPText
import com.kape.ui.mobile.text.TileTitleText
import com.kape.ui.utils.LocalColors
import org.koin.compose.koinInject

@Composable
fun IPTile(modifier: Modifier = Modifier) {
    val connectionPrefs: ConnectionPrefs = koinInject()
    val settingsPrefs: SettingsPrefs = koinInject()
    val connectionInfoProvider: ConnectionInfoProvider = koinInject()

    val publicIp by connectionPrefs.clientIp.collectAsStateWithLifecycle()
    val vpnIp by connectionPrefs.vpnIp.collectAsStateWithLifecycle()
    val isPortForwardingEnabled by settingsPrefs.isPortForwardingEnabled.collectAsStateWithLifecycle()
    val portForwardingStatus: PortForwardingStatus by connectionInfoProvider.portForwardingStatus.collectAsStateWithLifecycle()
    val port: String by connectionInfoProvider.port.collectAsStateWithLifecycle()

    Row(modifier = modifier.padding(horizontal = 32.dp, vertical = 16.dp)) {
        Column(
            Modifier
                .weight(0.4f)
                .semantics(mergeDescendants = true) {},
        ) {
            TileTitleText(content = stringResource(id = R.string.public_ip).uppercase())
            Spacer(modifier = Modifier.height(4.dp))
            IPText(content = publicIp)
        }

        Icon(
            painter = painterResource(id = R.drawable.ic_arrow),
            contentDescription = null,
            tint = LocalColors.current.onSurface,
            modifier =
                Modifier
                    .align(CenterVertically)
                    .weight(0.2f),
        )

        Column(
            Modifier
                .weight(0.4f)
                .semantics(mergeDescendants = true) {},
        ) {
            TileTitleText(content = stringResource(id = R.string.vpn_ip).uppercase())
            Spacer(modifier = Modifier.height(4.dp))
            IPText(content = vpnIp, ":Text:vpnIp")
            if (isPortForwardingEnabled) {
                Row {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_port_forwarding),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier =
                            Modifier
                                .size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = getPortForwardingStatusString(portForwardingStatus, port),
                        color = LocalColors.current.onSurface,
                        fontSize = 14.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun getPortForwardingStatusString(
    status: PortForwardingStatus,
    port: String,
): String =
    when (status) {
        PortForwardingStatus.Error -> stringResource(id = R.string.pfwd_error)
        PortForwardingStatus.NoPortForwarding -> stringResource(id = R.string.pfwd_disabled)
        PortForwardingStatus.Requesting -> stringResource(id = R.string.pfwd_requesting)
        PortForwardingStatus.Success -> port
    }