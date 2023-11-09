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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kape.connection.R
import com.kape.settings.data.ProtocolSettings
import com.kape.ui.text.ConnectionInfoText
import com.kape.ui.text.TileTitleText

@Composable
fun ConnectionInfo(settings: ProtocolSettings) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        TileTitleText(stringResource(id = R.string.connection))
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                InfoRow(iconId = R.drawable.ic_connection, label = settings.name)
                InfoRow(iconId = R.drawable.ic_port, label = settings.port)
                InfoRow(iconId = R.drawable.ic_authentication, label = settings.auth)
            }
            Column(modifier = Modifier.weight(1f)) {
                InfoRow(iconId = R.drawable.ic_socket, label = settings.transport.value)
                InfoRow(iconId = R.drawable.ic_encryption, label = settings.dataEncryption.value)
                InfoRow(iconId = R.drawable.ic_handshake, label = settings.handshake)
            }
        }
    }
}

@Composable
fun InfoRow(iconId: Int, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(id = iconId),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(24.dp),
        )
        Spacer(modifier = Modifier.width(4.dp))
        ConnectionInfoText(content = label)
    }
}