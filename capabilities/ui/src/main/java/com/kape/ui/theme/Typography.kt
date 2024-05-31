package com.kape.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

@Immutable
object PiaTypography {
    val h1: TextStyle = TextStyle(
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.0015.em,
        fontWeight = FontWeight.Medium,
    )
    val h2 = TextStyle(
        fontSize = 20.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.0015.em,
        fontWeight = FontWeight.Medium,
    )
    val h3 = TextStyle(
        fontSize = 20.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.0015.em,
        fontWeight = FontWeight.Light,
    )
    val subtitle1 = TextStyle(
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.001.em,
        fontWeight = FontWeight.Medium,
    )
    val subtitle2 = TextStyle(
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.001.em,
        fontWeight = FontWeight.Medium,
    )
    val subtitle3 = TextStyle(
        fontSize = 14.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.001.em,
        fontWeight = FontWeight.Medium,
    )
    val body1 = TextStyle(
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.005.em,
        fontWeight = FontWeight.Normal,
    )
    val body2 = TextStyle(
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.005.em,
        fontWeight = FontWeight.Light,
    )
    val body3 = TextStyle(
        fontSize = 14.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.0025.em,
        fontWeight = FontWeight.Normal,
    )
    val button1 = TextStyle(
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.0125.em,
        fontWeight = FontWeight.Medium,
    )
    val button2 = TextStyle(
        fontSize = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.0125.em,
        fontWeight = FontWeight.Medium,
    )
    val caption1 = TextStyle(
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.004.em,
        fontWeight = FontWeight.Normal,
    )
    val caption2 = TextStyle(
        fontSize = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.004.em,
        fontWeight = FontWeight.Light,
    )
}

val AppTypography = Typography(
    displayLarge = PiaTypography.h1,
    displayMedium = PiaTypography.h1,
    displaySmall = PiaTypography.h2,
    headlineLarge = PiaTypography.h2,
    headlineMedium = PiaTypography.h3,
    headlineSmall = PiaTypography.h3,
    titleLarge = PiaTypography.subtitle1,
    titleMedium = PiaTypography.subtitle2,
    titleSmall = PiaTypography.subtitle3,
    bodyLarge = PiaTypography.body1,
    bodyMedium = PiaTypography.body2,
    bodySmall = PiaTypography.body3,
    labelLarge = PiaTypography.subtitle3,
    labelMedium = PiaTypography.caption1,
    labelSmall = PiaTypography.caption2,
)