package com.kape.uicomponents.theme

import androidx.compose.ui.graphics.Color

val Purple200 = Color(0xFFBB86FC)
val Purple500 = Color(0xFF6200EE)
val Purple700 = Color(0xFF3700B3)
val Teal200 = Color(0xFF03DAC5)
val Grey20 = Color(0xFF323642)
val Grey55 = Color(0xFF889099)
val Grey92 = Color(0xFFEEEEEE)
val Grey85 = Color(0xFFD7D8D9)
val Transparent = Color(0xFF000000)
val DarkGreen50 = Color(0xFF037900)
val DarkGreen20 = Color(0xFF4CB647)
val ErrorRed = Color(0xFFF5515F)

val DisconnectedStart = Color(0xFFF5515F)
val DisconnectedEnd = Color(0xFFC82B3C)
val DisconnectedGradient = listOf(DisconnectedStart, DisconnectedEnd)
val ConnectingStart = Color(0xFFF9D001)
val ConnectingEnd = Color(0xFFE6B400)
val ConnectingGradient = listOf(ConnectingStart, ConnectingEnd)
val ConnectedStart = Color(0xFF5DDF5A)
val ConnectedEnd = Color(0xFF4CB649)
val ConnectedGradient = listOf(ConnectedStart, ConnectedEnd)

object Latency {
    val Green = ConnectedStart
    val Yellow = ConnectingStart
    val Red = DisconnectedStart
}
