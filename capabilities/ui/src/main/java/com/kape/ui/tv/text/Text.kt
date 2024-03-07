@file:OptIn(ExperimentalTvMaterial3Api::class)

package com.kape.ui.tv.text

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import com.kape.ui.theme.PiaTypography
import com.kape.ui.utils.LocalColors

@Composable
fun PrimaryButtonText(content: String) {
    Text(
        modifier = Modifier.fillMaxWidth(),
        text = content,
        textAlign = TextAlign.Center,
        color = LocalColors.current.onPrimary,
        style = PiaTypography.button1,
    )
}

@Composable
fun SecondaryButtonText(content: String) {
    Text(
        modifier = Modifier.fillMaxWidth(),
        text = content,
        textAlign = TextAlign.Center,
        color = LocalColors.current.onError,
        style = PiaTypography.button1,
    )
}

@Composable
fun WelcomeTitleText(content: String, modifier: Modifier) {
    Text(
        text = content,
        color = LocalColors.current.onSurface,
        style = PiaTypography.h1,
        fontSize = 32.sp,
        modifier = modifier,
    )
}

@Composable
fun EnterUsernameScreenTitleText(content: String, modifier: Modifier = Modifier) {
    Text(
        text = content,
        textAlign = TextAlign.Center,
        color = LocalColors.current.onSurface,
        style = PiaTypography.h1,
        fontSize = 32.sp,
        modifier = modifier,
    )
}

@Composable
fun OnboardingTitleText(content: String, modifier: Modifier) {
    androidx.compose.material3.Text(
        text = content,
        color = LocalColors.current.onSurface,
        style = PiaTypography.h2,
        textAlign = TextAlign.Start,
        modifier = modifier,
    )
}

@Composable
fun OnboardingDescriptionText(content: String, modifier: Modifier) {
    Text(
        text = content,
        color = LocalColors.current.onSurface,
        style = PiaTypography.body1,
        textAlign = TextAlign.Start,
        modifier = modifier,
    )
}

@Composable
fun OnboardingFooterText(content: String, modifier: Modifier) {
    Text(
        text = content,
        color = LocalColors.current.onSurfaceVariant,
        style = PiaTypography.caption1,
        textAlign = TextAlign.Start,
        modifier = modifier,
    )
}

@Composable
fun AppBarTitleText(
    content: String,
    textColor: Color,
    isError: Boolean,
    modifier: Modifier,
) {
    Text(
        modifier = modifier,
        text = content,
        textAlign = TextAlign.Center,
        color = if (isError) LocalColors.current.onPrimary else textColor,
        style = PiaTypography.h1,
    )
}