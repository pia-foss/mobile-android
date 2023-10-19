package com.kape.ui.text

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kape.ui.theme.PiaTypography
import com.kape.ui.utils.LocalColors

@Composable
fun SettingsL1Text(content: String, modifier: Modifier) {
    Text(
        text = content,
        color = LocalColors.current.onSurface,
        style = PiaTypography.subtitle3,
        modifier = modifier,
    )
}

@Composable
fun SideMenuUsernameText(content: String, modifier: Modifier) {
    Text(
        text = content,
        color = LocalColors.current.onSurface,
        style = PiaTypography.subtitle2,
        modifier = modifier,
    )
}

@Composable
fun SideMenuVersionText(content: String, modifier: Modifier) {
    Text(
        text = content,
        color = LocalColors.current.onSurface,
        style = PiaTypography.caption1,
        modifier = modifier,
    )
}

@Composable
fun AppBarConnectionTextDefault(content: String, modifier: Modifier) {
    Text(
        text = content,
        color = LocalColors.current.onSurface,
        style = PiaTypography.subtitle3,
        modifier = modifier,
    )
}

@Composable
fun AppBarConnectionTextError(content: String, modifier: Modifier) {
    Text(
        text = content,
        color = LocalColors.current.onPrimary,
        style = PiaTypography.subtitle3,
        modifier = modifier,
    )
}

@Composable
fun AppBarTitleText(content: String, modifier: Modifier) {
    Text(
        text = content,
        color = LocalColors.current.onSurface,
        style = PiaTypography.h2,
        modifier = modifier,
    )
}

@Composable
fun SettingsL2Text(content: String, modifier: Modifier) {
    Text(
        text = content,
        color = LocalColors.current.onSurface,
        style = PiaTypography.subtitle3,
        modifier = modifier,
    )
}

@Composable
fun SettingsL2TextDescription(content: String, modifier: Modifier) {
    Text(
        text = content,
        color = LocalColors.current.onSurfaceVariant,
        style = PiaTypography.caption1,
        modifier = modifier,
    )
}

@Composable
fun SignInTitleText(content: String, modifier: Modifier) {
    Text(
        text = content,
        color = LocalColors.current.onSurface,
        style = PiaTypography.subtitle1,
        modifier = modifier,
    )
}

@Composable
fun SignInDescriptionText(content: String, modifier: Modifier) {
    Text(
        text = content,
        color = LocalColors.current.onSurface,
        style = PiaTypography.body3,
        modifier = modifier,
    )
}

@Composable
fun SignUpDurationText(content: String, modifier: Modifier) {
    Text(
        text = content,
        color = LocalColors.current.onSurfaceVariant,
        style = PiaTypography.body3,
        modifier = modifier,
    )
}

@Composable
fun SignUpPriceText(content: String, modifier: Modifier) {
    Text(
        text = content,
        color = LocalColors.current.onSurface,
        style = PiaTypography.subtitle1,
        modifier = modifier,
    )
}

@Composable
fun SignUpPricePerMonthText(content: String, modifier: Modifier) {
    Text(
        text = content,
        color = LocalColors.current.onSurfaceVariant,
        style = PiaTypography.body3,
        modifier = modifier,
    )
}

@Composable
fun BestValueBannerText(content: String, modifier: Modifier) {
    Text(
        text = content,
        color = LocalColors.current.surface,
        style = PiaTypography.caption1,
        modifier = modifier,
    )
}

@Composable
fun PrimaryButtonText(content: String, modifier: Modifier) {
    Text(
        text = content,
        color = LocalColors.current.onPrimary,
        style = PiaTypography.button1,
        modifier = modifier,
    )
}

@Composable
fun SecondaryButtonText(content: String, modifier: Modifier) {
    Text(
        text = content,
        color = LocalColors.current.primary,
        style = PiaTypography.button1,
        modifier = modifier,
    )
}

@Composable
fun ErrorText(content: String, modifier: Modifier) {
    Text(
        text = content,
        color = LocalColors.current.error,
        style = PiaTypography.body3,
        modifier = modifier,
    )
}

@Composable
fun OnboardingTitleText(content: String, modifier: Modifier) {
    Text(
        text = content,
        color = LocalColors.current.onSurface,
        style = PiaTypography.h2,
        modifier = modifier,
    )
}

@Composable
fun OnboardingDescriptionText(content: String, modifier: Modifier) {
    Text(
        text = content,
        color = LocalColors.current.onSurface,
        style = PiaTypography.body1,
        modifier = modifier,
    )
}

@Composable
fun OnboardingFooterText(content: String, modifier: Modifier) {
    Text(
        text = content,
        color = LocalColors.current.onSurfaceVariant,
        style = PiaTypography.caption1,
        modifier = modifier,
    )
}

@Composable
fun SelectedRegionTitleText(content: String, modifier: Modifier) {
    Text(
        text = content,
        color = LocalColors.current.onSurface,
        style = PiaTypography.caption2,
        modifier = modifier,
    )
}

@Composable
fun SelectedRegionServerText(content: String, modifier: Modifier) {
    Text(
        text = content,
        color = LocalColors.current.onSurface,
        style = PiaTypography.subtitle3,
        modifier = modifier,
    )
}

@Composable
fun QuickConnectText(content: String, modifier: Modifier) {
    Text(
        text = content,
        color = LocalColors.current.onSurface,
        style = PiaTypography.caption1,
        modifier = modifier,
    )
}

@Composable
fun TileTitleText(content: String, modifier: Modifier) {
    Text(
        text = content,
        color = LocalColors.current.onSurfaceVariant,
        style = PiaTypography.button2,
        modifier = modifier,
    )
}

@Composable
fun IPText(content: String, modifier: Modifier) {
    Text(
        text = content,
        color = LocalColors.current.onSurface,
        style = PiaTypography.body3,
        modifier = modifier,
    )
}

@Composable
fun ConnectionInfoText(content: String, modifier: Modifier) {
    Text(
        text = content,
        color = LocalColors.current.onSurface,
        style = PiaTypography.body3,
        modifier = modifier,
    )
}

@Composable
fun RegionSelectionText(content: String, modifier: Modifier) {
    Text(
        text = content,
        color = LocalColors.current.onSurface,
        style = PiaTypography.body3,
        modifier = modifier,
    )
}

@Composable
fun InputLabelText(content: String) {
    Text(
        text = content,
        color = LocalColors.current.onSurfaceVariant,
        style = PiaTypography.caption1,
    )
}

@Composable
fun InputErrorText(content: String, modifier: Modifier) {
    Text(
        text = content,
        color = LocalColors.current.error,
        style = PiaTypography.caption1,
        modifier = modifier,
    )
}