package com.kape.connection.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.kape.connection.R
import com.kape.ui.theme.Grey92
import com.kape.ui.theme.Space
import com.kape.ui.theme.Square
import com.kape.ui.theme.appbarConnectedStatus
import com.kape.ui.theme.appbarConnectingStatus
import com.kape.ui.theme.appbarDisconnectedStatus

@Composable
fun ConnectionButton(state: ConnectionState, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        if (state.showProgress) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(Square.CONNECTION_PROGRESS)
                    .align(Alignment.Center),
                color = state.color,
                strokeWidth = Space.SMALL
            )
        }
        IconButton(
            modifier = Modifier
                .align(Alignment.Center)
                .size(Square.CONNECTION_PROGRESS),
            onClick = onClick
        ) {
            Icon(
                painter = painterResource(id = state.resId),
                contentDescription = stringResource(com.kape.ui.R.string.button),
                tint = Color.Unspecified,
                modifier = Modifier.size(Square.CONNECTION_IMAGE)
            )
        }
    }
}

sealed class ConnectionState(val color: Color, val resId: Int, val showProgress: Boolean) {
    data object Default : ConnectionState(Grey92, R.drawable.ic_connection_off, false)
    data object Connecting : ConnectionState(appbarConnectingStatus, R.drawable.ic_connecting, true)
    data object Connected :
        ConnectionState(appbarConnectedStatus, R.drawable.ic_connection_on, false)

    data object Disconnected :
        ConnectionState(appbarDisconnectedStatus, R.drawable.ic_connection_error, false)
}

@Preview
@Composable
fun DefaultConnectionButtonPreview() {
    ConnectionButton(state = ConnectionState.Default) {}
}