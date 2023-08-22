package com.kape.connection.ui.tiles

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kape.connection.R
import com.kape.ui.elements.ConnectionTile
import com.kape.utils.server.Server

@Composable
fun QuickConnectTile(servers: List<Server>) {
    ConnectionTile(labelId = R.string.quick_connect) {
        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (servers.isEmpty()) {
                for (index in 1..MAX_SERVERS) {
                    ServerTile(modifier = Modifier.weight(1f))
                }
            } else {
                for (index in 0 until MAX_SERVERS) {
                    if (servers.getOrNull(index) != null) {
                        ServerTile(
                            server = servers[index],
                            modifier = Modifier.weight(1f),
                        )
                    } else {
                        ServerTile(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}