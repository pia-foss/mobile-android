package com.kape.connection.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kape.connection.R
import com.kape.ui.theme.Square
import com.kape.ui.theme.statusBarConnected
import com.kape.ui.theme.statusBarConnecting
import com.kape.ui.theme.statusBarDefault
import com.kape.ui.utils.LocalColors
import com.kape.vpnconnect.utils.ConnectionStatus

@Composable
fun ConnectionButton(status: ConnectionStatus, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
    ) {
        val state = getConnectionState(status, LocalColors.current)
        if (state.showProgress) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(Square.CONNECTION_PROGRESS)
                    .align(Alignment.Center),
                color = state.color,
                strokeWidth = 8.dp,
            )
        }
        IconButton(
            modifier = Modifier
                .align(Alignment.Center)
                .size(Square.CONNECTION_PROGRESS),
            onClick = onClick,
        ) {
            Icon(
                painter = painterResource(id = state.resId),
                contentDescription = stringResource(com.kape.ui.R.string.button),
                tint = Color.Unspecified,
                modifier = Modifier.size(Square.CONNECTION_IMAGE),
            )
        }
    }
}

private data class ConnectionState(val color: Color, val resId: Int, val showProgress: Boolean)

private fun getConnectionState(status: ConnectionStatus, scheme: ColorScheme): ConnectionState {
    return when (status) {
        ConnectionStatus.CONNECTED -> ConnectionState(
            color = scheme.statusBarConnected(),
            resId = R.drawable.ic_connection_on,
            showProgress = false,
        )

        ConnectionStatus.CONNECTING,
        ConnectionStatus.RECONNECTING,
        -> ConnectionState(
            color = scheme.statusBarConnecting(),
            resId = R.drawable.ic_connecting,
            showProgress = true,
        )

        ConnectionStatus.DISCONNECTED -> ConnectionState(
            color = scheme.statusBarDefault(),
            resId = R.drawable.ic_connection_off,
            showProgress = false,
        )
    }
}