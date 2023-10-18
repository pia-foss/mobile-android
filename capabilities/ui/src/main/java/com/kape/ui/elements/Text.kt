package com.kape.ui.elements

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.kape.ui.theme.PiaTypography
import com.kape.ui.utils.LocalColors

@Composable
fun SettingsL1Text(content: String) {
    Text(text = content, color = LocalColors.current.onSurface, style = PiaTypography.subtitle3)
}

@Composable
fun SideMenuUsernameText(content: String) {
    Text(text = content, color = LocalColors.current.onSurface, style = PiaTypography.subtitle2)
}

@Composable
fun SideMenuVersionText(content: String) {
    Text(text = content, color = LocalColors.current.onSurface, style = PiaTypography.caption1)
}

@Composable
fun AppBarConnectionTextDefault(content: String) {
    Text(text = content, color = LocalColors.current.onSurface, style = PiaTypography.subtitle3)
}

@Composable
fun AppBarConnectionTextError(content: String) {
    Text(text = content, color = LocalColors.current.onPrimary, style = PiaTypography.subtitle3)
}

@Composable
fun AppBarTitleText(content: String) {
    Text(text = content, color = LocalColors.current.onSurface, style = PiaTypography.h2)
}

@Composable
fun SettingsL2Text(content: String) {
    Text(text = content, color = LocalColors.current.onSurface, style = PiaTypography.subtitle3)
}

@Composable
fun SettingsL2TextDescription(content: String) {
    Text(
        text = content,
        color = LocalColors.current.onSurfaceVariant,
        style = PiaTypography.caption1,
    )
}

@Composable
fun SignInTitleText(content: String) {
    Text(text = content, color = LocalColors.current.onSurface, style = PiaTypography.subtitle1)
}

@Composable
fun SignInDescriptionText(content: String) {
    Text(text = content, color = LocalColors.current.onSurface, style = PiaTypography.body3)
}

@Composable
fun SignInDurationText(content: String) {
    Text(text = content, color = LocalColors.current.onSurfaceVariant, style = PiaTypography.body3)
}

@Composable
fun SignUpPriceText(content: String) {
    Text(text = content, color = LocalColors.current.onSurface, style = PiaTypography.subtitle1)
}

@Composable
fun SignUpPricePerMonthText(content: String) {
    Text(text = content, color = LocalColors.current.onSurfaceVariant, style = PiaTypography.body3)
}

@Composable
fun BestValueBannerText(content: String) {
    Text(text = content, color = LocalColors.current.onPrimary, style = PiaTypography.caption1)
}

@Composable
fun PrimaryButtonText(content: String) {
    Text(text = content, color = LocalColors.current.onPrimary, style = PiaTypography.button1)
}

@Composable
fun SecondaryButtonText(content: String) {
    Text(text = content, color = LocalColors.current.primary, style = PiaTypography.button1)
}

@Composable
fun ErrorText(content: String) {
    Text(text = content, color = LocalColors.current.error, style = PiaTypography.body3)
}

@Composable
fun OnboardingTitleText(content: String) {
    Text(text = content, color = LocalColors.current.onSurface, style = PiaTypography.h2)
}

@Composable
fun OnboardingDescriptionText(content: String) {
    Text(text = content, color = LocalColors.current.onSurface, style = PiaTypography.body1)
}

@Composable
fun OnboardingFooterText(content: String) {
    Text(
        text = content,
        color = LocalColors.current.onSurfaceVariant,
        style = PiaTypography.caption1,
    )
}

@Composable
fun SelectedRegionTitleText(content: String) {
    Text(text = content, color = LocalColors.current.onSurface, style = PiaTypography.caption2)
}

@Composable
fun SelectedRegionServerText(content: String) {
    Text(text = content, color = LocalColors.current.onSurface, style = PiaTypography.subtitle3)
}

@Composable
fun QuickConnectText(content: String) {
    Text(text = content, color = LocalColors.current.onSurface, style = PiaTypography.caption1)
}

@Composable
fun TileTitleText(content: String) {
    Text(
        text = content,
        color = LocalColors.current.onSurfaceVariant,
        style = PiaTypography.button2,
    )
}

@Composable
fun IPText(content: String) {
    Text(text = content, color = LocalColors.current.onSurface, style = PiaTypography.body3)
}

@Composable
fun ConnectionInfoText(content: String) {
    Text(text = content, color = LocalColors.current.onSurface, style = PiaTypography.body3)
}

@Composable
fun RegionSelectionText(content: String) {
    Text(text = content, color = LocalColors.current.onSurface, style = PiaTypography.body3)
}