package com.kape.connection.ui

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.kape.connection.R
import com.kape.ui.theme.connectionDefault
import com.kape.ui.theme.connectionError
import com.kape.ui.utils.LocalColors
import com.kape.vpnconnect.utils.ConnectionStatus

@Composable
fun ConnectButton(status: ConnectionStatus, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(color = getStatusColor(status = status)),
            ) { onClick() },
    ) {
        when (status) {
            ConnectionStatus.CONNECTED,
            ConnectionStatus.DISCONNECTED,
            ConnectionStatus.DISCONNECTING,
            ConnectionStatus.ERROR,
            -> ButtonBackground(color = getStatusColor(status = status))

            ConnectionStatus.CONNECTING,
            ConnectionStatus.RECONNECTING,
            -> LoadingButtonBackground(color = getStatusColor(status = status))
        }
        Icon(
            painter = painterResource(id = R.drawable.ic_power),
            contentDescription = null,
            tint = getStatusColor(status),
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

@Composable
private fun getStatusColor(status: ConnectionStatus): Color {
    return when (status) {
        ConnectionStatus.CONNECTED -> LocalColors.current.primary
        ConnectionStatus.DISCONNECTED,
        ConnectionStatus.DISCONNECTING,
        ConnectionStatus.CONNECTING,
        ConnectionStatus.RECONNECTING,
        -> LocalColors.current.connectionDefault()

        ConnectionStatus.ERROR -> LocalColors.current.connectionError()
    }
}

@Composable
private fun ButtonBackground(color: Color, progress: Float = 360f) {
    val surfaceColor = LocalColors.current.surface

    Canvas(
        modifier = Modifier.size(160.dp),
        onDraw = {
            drawCircle(color = color.copy(alpha = 0.1f))
            drawArc(
                startAngle = 270f, // 270 is 0 degree
                sweepAngle = progress,
                useCenter = false,
                color = color,
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round),
                size = Size(140.dp.toPx(), 140.dp.toPx()),
                topLeft = Offset(10.dp.toPx(), 10.dp.toPx()),
            )
            drawCircle(surfaceColor, radius = 67.dp.toPx(), center)
        },
    )
}

@Composable
private fun LoadingButtonBackground(color: Color) {
    val surfaceColor = LocalColors.current.surface

    val endAnimation by rememberInfiniteTransition().animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
        ),
    )
    val startAnimation by rememberInfiniteTransition().animateFloat(
        initialValue = 270f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
        ),
    )
    Canvas(
        modifier = Modifier.size(160.dp),
        onDraw = {
            drawCircle(color = color.copy(alpha = 0.1f))
            drawArc(
                startAngle = startAnimation, // 270 is 0 degree
                sweepAngle = endAnimation,
                useCenter = false,
                color = color,
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round),
                size = Size(140.dp.toPx(), 140.dp.toPx()),
                topLeft = Offset(10.dp.toPx(), 10.dp.toPx()),
            )
            drawCircle(surfaceColor, radius = 67.dp.toPx(), center)
        },
    )
}