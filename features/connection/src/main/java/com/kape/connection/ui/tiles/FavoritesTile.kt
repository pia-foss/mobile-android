package com.kape.connection.ui.tiles

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kape.connection.R
import com.kape.ui.elements.ConnectionTile
import com.kape.utils.server.VpnServer

const val MAX_SERVERS = 6

@Composable
fun FavoritesTile(favoriteServers: List<VpnServer>) {
    ConnectionTile(labelId = R.string.favorite_servers) {
        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (favoriteServers.isEmpty()) {
                for (index in 1..MAX_SERVERS) {
                    ServerTile(modifier = Modifier.weight(1f))
                }
            } else {
                for (index in 0 until MAX_SERVERS) {
                    if (favoriteServers.getOrNull(index) != null) {
                        ServerTile(
                            server = favoriteServers[index],
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