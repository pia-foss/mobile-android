package com.kape.connection.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.kape.connection.R
import com.kape.uicomponents.theme.*

@Composable
fun ConnectionInfoTile() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Space.NORMAL)
    ) {
        Text(
            text = stringResource(id = R.string.connection),
            color = Grey55,
            fontSize = FontSize.Small
        )
        Spacer(modifier = Modifier.height(Space.NORMAL))
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
            contentDescription = stringResource(
                id = com.kape.sidemenu.R.string.icon
            ), tint = Color.Unspecified, modifier = Modifier.size(Square.CONNECTION_INFO)
        )
        Spacer(modifier = Modifier.width(Space.MINI))
        Text(text = label, fontSize = FontSize.Normal, color = Grey20)
    }
}