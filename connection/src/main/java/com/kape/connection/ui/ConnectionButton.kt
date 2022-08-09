package com.kape.connection.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.kape.connection.R
import com.kape.uicomponents.theme.*

@Composable
fun ConnectionButton(state: ConnectionState) {

    Box(modifier = Modifier.fillMaxWidth()) {
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
            modifier = Modifier.align(Alignment.Center),
            onClick = {
                // TODO: implement
            }
        ) {
            Icon(
                painter = painterResource(id = state.resId),
                contentDescription = stringResource(com.kape.uicomponents.R.string.button),
                tint = Color.Unspecified,
                modifier = Modifier.size(Square.CONNECTION_IMAGE)
            )
        }
    }

}

sealed class ConnectionState(val color: Color, val resId: Int, val showProgress: Boolean) {
    object Default : ConnectionState(Grey92, R.drawable.ic_connection_off, false)
    object Connecting : ConnectionState(ConnectingEnd, R.drawable.ic_connecting, true)
    object Connected : ConnectionState(DarkGreen20, R.drawable.ic_connection_on, false)
    object Disconnected : ConnectionState(ErrorRed, R.drawable.ic_connection_error, false)
}