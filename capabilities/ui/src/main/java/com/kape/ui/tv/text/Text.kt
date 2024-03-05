package com.kape.ui.tv.text

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import com.kape.ui.theme.PiaTypography
import com.kape.ui.utils.LocalColors

@OptIn(ExperimentalTvMaterial3Api::class)
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

@OptIn(ExperimentalTvMaterial3Api::class)
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

@OptIn(ExperimentalTvMaterial3Api::class)
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

@OptIn(ExperimentalTvMaterial3Api::class)
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