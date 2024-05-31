package com.kape.ui.utils

import androidx.compose.ui.graphics.Color

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