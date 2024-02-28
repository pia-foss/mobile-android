package com.kape.ui.mobile.tiles

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kape.ui.R
import com.kape.ui.mobile.text.ConnectionInfoText
import com.kape.ui.mobile.text.TileTitleText
import com.kape.ui.utils.LocalColors

@Composable
fun ConnectionInfo(
    modifier: Modifier = Modifier,
    connection: String,
    port: String,
    auth: String,
    transport: String,
    encryption: String,
    handshake: String,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 16.dp),
    ) {
        TileTitleText(stringResource(id = R.string.connection))
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                InfoRow(iconId = R.drawable.ic_connection, label = connection)
                InfoRow(iconId = R.drawable.ic_port, label = port)
                InfoRow(iconId = R.drawable.ic_authentication, label = auth)
            }
            Column(modifier = Modifier.weight(1f)) {
                InfoRow(iconId = R.drawable.ic_socket, label = transport)
                InfoRow(iconId = R.drawable.ic_encryption, label = encryption)
                InfoRow(iconId = R.drawable.ic_handshake, label = handshake)
            }
        }
    }
}

@Composable
fun InfoRow(iconId: Int, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 8.dp),
    ) {
        Icon(
            painter = painterResource(id = iconId),
            contentDescription = null,
            tint = LocalColors.current.onSurface,
            modifier = Modifier.size(24.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        ConnectionInfoText(content = label)
    }
}