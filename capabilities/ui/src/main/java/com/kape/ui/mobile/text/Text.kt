package com.kape.ui.mobile.text

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kape.ui.theme.PiaTypography
import com.kape.ui.theme.infoBlue
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
fun AppBarConnectionTextDefault(content: String, isError: Boolean, modifier: Modifier) {
    Text(
        text = content,
        color = if (isError) LocalColors.current.onPrimary else LocalColors.current.onSurface,
        style = PiaTypography.subtitle3,
        modifier = modifier,
    )
}

@Composable
fun AppBarTitleText(content: String, isError: Boolean, modifier: Modifier) {
    Text(
        text = content,
        color = if (isError) LocalColors.current.onPrimary else LocalColors.current.onSurface,
        style = PiaTypography.h2,
        modifier = modifier,
    )
}

@Composable
fun SettingsL2Text(content: String) {
    Text(
        text = content,
        color = LocalColors.current.onSurface,
        style = PiaTypography.subtitle3,
    )
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
fun SignInText(content: String, modifier: Modifier) {
    Text(
        text = content,
        color = LocalColors.current.onSurface,
        style = PiaTypography.h2,
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
        color = LocalColors.current.onTertiary,
        style = PiaTypography.caption1,
        modifier = modifier,
    )
}

@Composable
fun PrimaryButtonText(content: String) {
    Text(
        text = content,
        color = LocalColors.current.onPrimary,
        style = PiaTypography.button1,
    )
}

@Composable
fun SecondaryButtonText(content: String) {
    Text(
        text = content,
        color = LocalColors.current.primary,
        style = PiaTypography.button1,
    )
}

@Composable
fun TertiaryButtonText(content: String) {
    Text(
        text = content,
        style = PiaTypography.button2,
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
fun InfoText(content: String, modifier: Modifier) {
    Text(
        text = content,
        color = LocalColors.current.infoBlue(),
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
        textAlign = TextAlign.Center,
        modifier = modifier,
    )
}

@Composable
fun OnboardingDescriptionText(content: String, modifier: Modifier) {
    Text(
        text = content,
        color = LocalColors.current.onSurface,
        style = PiaTypography.body1,
        textAlign = TextAlign.Center,
        modifier = modifier,
    )
}

@Composable
fun OnboardingFooterText(content: String, modifier: Modifier) {
    Text(
        text = content,
        color = LocalColors.current.onSurfaceVariant,
        style = PiaTypography.caption1,
        textAlign = TextAlign.Center,
        modifier = modifier,
    )
}

@Composable
fun SelectedRegionTitleText(content: String) {
    Text(
        text = content,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        color = LocalColors.current.onSurface,
        style = PiaTypography.caption2,
    )
}

@Composable
fun SelectedRegionServerText(content: String) {
    Text(
        text = content,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        color = LocalColors.current.onSurface,
        style = PiaTypography.subtitle3,
    )
}

@Composable
fun QuickConnectText(content: String, modifier: Modifier) {
    Text(
        text = content,
        textAlign = TextAlign.Center,
        color = LocalColors.current.onSurface,
        style = PiaTypography.caption1,
        modifier = modifier,
    )
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
    Text(
        text = content,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        color = LocalColors.current.onSurface,
        style = PiaTypography.body3,
    )
}

@Composable
fun ConnectionInfoText(content: String) {
    Text(
        text = content,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        color = LocalColors.current.onSurface,
        style = PiaTypography.body3,
    )
}

@Composable
fun RegionSelectionText(content: String) {
    Text(
        text = content,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        color = LocalColors.current.onSurface,
        style = PiaTypography.body3,
    )
}

@Composable
fun RegionSelectionIpText(content: String) {
    Text(
        text = content,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        color = LocalColors.current.onSurfaceVariant,
        style = PiaTypography.body3,
    )
}

@Composable
fun RegionSelectionDipText(content: String) {
    Card(
        border = BorderStroke(1.dp, LocalColors.current.onSurfaceVariant),
        shape = RoundedCornerShape(16.dp),
    ) {
        Text(
            text = content,
            color = LocalColors.current.onSurfaceVariant,
            style = PiaTypography.caption2,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

@Composable
fun RegionSelectionLatencyText(content: String?, modifier: Modifier) {
    Text(
        text = "$content",
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
fun SearchInputLabelText(content: String) {
    Text(
        text = content,
        color = LocalColors.current.onSurfaceVariant,
        style = PiaTypography.body2,
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

@Composable
fun MenuText(content: String, modifier: Modifier) {
    Text(
        text = content,
        color = LocalColors.current.onSurface,
        style = PiaTypography.subtitle3,
        modifier = modifier,
    )
}

@Composable
fun Hyperlink(content: String, modifier: Modifier) {
    Text(
        text = content,
        color = LocalColors.current.primary,
        style = PiaTypography.subtitle3,
        modifier = modifier,
    )
}

@Composable
fun HyperlinkRed(content: String, modifier: Modifier) {
    Text(
        text = content,
        color = LocalColors.current.error,
        style = PiaTypography.subtitle3,
        textDecoration = TextDecoration.Underline,
        modifier = modifier,
    )
}

@Composable
fun DialogTitleText(content: String, modifier: Modifier) {
    Text(
        text = content,
        color = LocalColors.current.onSurface,
        style = PiaTypography.h2,
        modifier = modifier,
    )
}

@Composable
fun DialogMessageText(content: String, modifier: Modifier) {
    Text(
        text = content,
        color = LocalColors.current.onSurface,
        style = PiaTypography.body2,
        modifier = modifier,
    )
}

@Composable
fun DialogActionText(content: String, modifier: Modifier) {
    Text(
        text = content.uppercase(),
        color = LocalColors.current.primary,
        style = PiaTypography.subtitle3,
        modifier = modifier,
    )
}

@Composable
fun DedicatedIpHomeBannerText(content: String, modifier: Modifier = Modifier) {
    Text(
        modifier = modifier,
        text = content,
        color = LocalColors.current.surfaceVariant,
        style = PiaTypography.caption1,
        textAlign = TextAlign.Start,
    )
}