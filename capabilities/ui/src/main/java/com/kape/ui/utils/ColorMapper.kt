package com.kape.ui.utils

import androidx.compose.ui.graphics.Color
import com.kape.ui.theme.Latency

@Deprecated("to be removed. Already implemented in PiaColors.")
fun getLatencyTextColor(latency: String?): Color {
    if (latency == null) {
        return Color.White
    }
    return when (latency.toLong()) {
        in 0..200 -> Latency.Green
        in 200..500 -> Latency.Yellow
        else -> Latency.Red
    }
}

fun String.toColor(): Color {
    return Color(this.toULong())
}

fun Color.toColorString(): String {
    return this.value.toString()
}

fun defaultWidgetBackgroundColor(): Color = Color(0xFFFAFAFA)
fun defaultWidgetTextColor(): Color = Color(0x99001b31)
fun defaultWidgetUploadColor(): Color = Color(0xFF29CC41)
fun defaultWidgetDownloadColor(): Color = Color(0xFFF7941D)