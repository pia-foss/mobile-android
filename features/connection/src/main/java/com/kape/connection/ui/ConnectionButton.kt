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
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
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
fun ConnectButton(
    status: ConnectionStatus,
    onTvLayout: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val colorScheme = LocalColors.current
    val hasFocus = remember { mutableStateOf(false) }

    var backgroundColor = colorScheme.surface
    val color = when (status) {
        ConnectionStatus.ERROR,
        -> colorScheme.connectionError()
        ConnectionStatus.CONNECTED,
        -> colorScheme.primary
        ConnectionStatus.DISCONNECTED,
        ConnectionStatus.DISCONNECTING,
        ConnectionStatus.CONNECTING,
        ConnectionStatus.RECONNECTING,
        -> colorScheme.connectionDefault()
    }

    // If it's focused. We want to invert them.
    val targetColor = if (hasFocus.value) {
        val updatedColor = backgroundColor
        backgroundColor = color
        updatedColor
    } else {
        color
    }

    Box(
        modifier = modifier
            .clip(CircleShape)
            .onFocusChanged {
                if (onTvLayout) {
                    hasFocus.value = it.hasFocus
                }
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(
                    color = targetColor,
                ),
            ) { onClick() },
    ) {
        when (status) {
            ConnectionStatus.CONNECTED,
            ConnectionStatus.DISCONNECTED,
            ConnectionStatus.DISCONNECTING,
            ConnectionStatus.ERROR,
            -> ButtonBackground(
                loadingColor = color, // Intentional. The arc color stays the same on all scenarios.
                backgroundColor = backgroundColor,
            )

            ConnectionStatus.CONNECTING,
            ConnectionStatus.RECONNECTING,
            -> LoadingButtonBackground(
                loadingColor = color, // Intentional. The arc color stays the same on all scenarios.
                backgroundColor = backgroundColor,
            )
        }
        Icon(
            painter = painterResource(id = R.drawable.ic_power),
            contentDescription = null,
            tint = targetColor,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

@Composable
private fun ButtonBackground(
    loadingColor: Color,
    backgroundColor: Color,
    progress: Float = 360f,
) {
    Canvas(
        modifier = Modifier.size(160.dp),
        onDraw = {
            drawCircle(color = loadingColor.copy(alpha = 0.1f))
            drawArc(
                startAngle = 270f, // 270 is 0 degree
                sweepAngle = progress,
                useCenter = false,
                color = loadingColor,
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round),
                size = Size(140.dp.toPx(), 140.dp.toPx()),
                topLeft = Offset(10.dp.toPx(), 10.dp.toPx()),
            )
            drawCircle(backgroundColor, radius = 69.dp.toPx(), center)
        },
    )
}

@Composable
private fun LoadingButtonBackground(
    loadingColor: Color,
    backgroundColor: Color,
) {
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
            drawCircle(color = loadingColor.copy(alpha = 0.1f))
            drawArc(
                startAngle = startAnimation, // 270 is 0 degree
                sweepAngle = endAnimation,
                useCenter = false,
                color = loadingColor,
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round),
                size = Size(140.dp.toPx(), 140.dp.toPx()),
                topLeft = Offset(10.dp.toPx(), 10.dp.toPx()),
            )
            drawCircle(backgroundColor, radius = 69.dp.toPx(), center)
        },
    )
}