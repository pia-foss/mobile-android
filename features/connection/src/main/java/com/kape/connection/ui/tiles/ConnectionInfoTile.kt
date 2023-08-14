package com.kape.connection.ui.tiles

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.kape.connection.R
import com.kape.ui.elements.ConnectionTile
import com.kape.ui.theme.FontSize
import com.kape.ui.theme.Space
import com.kape.ui.theme.Square
import com.kape.ui.utils.LocalColors

@Composable
fun ConnectionInfoTile() {
    ConnectionTile(labelId = R.string.connection) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            // TODO: use real data; dummy data provided for display purposes only
            Column(modifier = Modifier.weight(1f)) {
                InfoRow(iconId = R.drawable.ic_connection, label = "OpenVPN")
                InfoRow(iconId = R.drawable.ic_port, label = "-")
                InfoRow(iconId = R.drawable.ic_authentication, label = "SHA256")
            }
            Column(modifier = Modifier.weight(1f)) {
                InfoRow(iconId = R.drawable.ic_socket, label = "UDP")
                InfoRow(iconId = R.drawable.ic_encryption, label = "AES-128-GCM")
                InfoRow(iconId = R.drawable.ic_handshake, label = "rsa4096")
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
            modifier = Modifier.size(Square.CONNECTION_INFO)
        )
        Spacer(modifier = Modifier.width(Space.MINI))
        Text(text = label, fontSize = FontSize.Normal, color = LocalColors.current.onSurface)
    }
}