package com.kape.connection.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.kape.connection.R
import com.kape.ui.theme.connectionDefault
import com.kape.ui.utils.LocalColors
import com.kape.vpnconnect.utils.ConnectionStatus

@Composable
fun ConnectButton(status: ConnectionStatus, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .size(160.dp)
            .background(
                Brush.radialGradient(
                    listOf(
                        getStatusColor(status),
                        getStatusColor(status),
                    ),
                ),
                CircleShape,
                0.1f,
            )
            .clip(CircleShape),
    ) {
        Box(
            modifier = Modifier
                .size(144.dp)
                .clip(CircleShape)
                .background(LocalColors.current.surface)
                .align(
                    Alignment.Center,
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = rememberRipple(color = getStatusColor(status = status)),
                ) { onClick() },
        ) {
            when (status) {
                ConnectionStatus.CONNECTED,
                ConnectionStatus.DISCONNECTED,
                -> {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(144.dp)
                            .align(Alignment.Center),
                        color = getStatusColor(status),
                        strokeWidth = 4.dp,
                        progress = 100f,
                    )
                }

                ConnectionStatus.CONNECTING,
                ConnectionStatus.RECONNECTING,
                -> {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(144.dp)
                            .align(Alignment.Center),
                        color = getStatusColor(status),
                        strokeWidth = 4.dp,
                    )
                }
            }

            Icon(
                painter = painterResource(id = R.drawable.ic_power),
                contentDescription = null,
                tint = getStatusColor(status),
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }
}

@Composable
private fun getStatusColor(status: ConnectionStatus): Color {
    return when (status) {
        ConnectionStatus.CONNECTED -> LocalColors.current.primary
        ConnectionStatus.DISCONNECTED,
        ConnectionStatus.CONNECTING,
        ConnectionStatus.RECONNECTING,
        -> LocalColors.current.connectionDefault()
    }
}