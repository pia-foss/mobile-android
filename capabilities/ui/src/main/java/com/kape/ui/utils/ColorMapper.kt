package com.kape.ui.utils

import androidx.compose.ui.graphics.Color
import com.kape.ui.theme.Latency

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