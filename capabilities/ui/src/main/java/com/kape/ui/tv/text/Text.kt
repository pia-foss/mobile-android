@file:OptIn(ExperimentalTvMaterial3Api::class)

package com.kape.ui.tv.text

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import com.kape.ui.theme.PiaTypography
import com.kape.ui.utils.LocalColors

@Composable
fun PrimaryButtonText(
    content: String,
    textAlign: TextAlign = TextAlign.Center,
) {
    Text(
        modifier = Modifier.fillMaxWidth(),
        text = content,
        textAlign = textAlign,
        style = PiaTypography.button1,
    )
}

@Composable
fun SecondaryButtonText(
    modifier: Modifier = Modifier,
    content: String,
    textAlign: TextAlign = TextAlign.Center,
) {
    Text(
        modifier = modifier.fillMaxWidth(),
        text = content,
        textAlign = textAlign,
        style = PiaTypography.button1,
    )
}

@Composable
fun TertiaryButtonText(
    content: String,
    textAlign: TextAlign = TextAlign.Center,
) {
    Text(
        modifier = Modifier.fillMaxWidth(),
        text = content,
        textAlign = textAlign,
        style = PiaTypography.button2,
    )
}

@Composable
fun PrimaryTabText(content: String, color: Color = LocalColors.current.onPrimary) {
    Text(
        modifier = Modifier.fillMaxWidth(),
        text = content,
        textAlign = TextAlign.Center,
        color = color,
        style = PiaTypography.button1,
    )
}

@Composable
fun SecondaryTabText(content: String, color: Color = LocalColors.current.onSurface) {
    Text(
        modifier = Modifier.fillMaxWidth(),
        text = content,
        textAlign = TextAlign.Center,
        color = color,
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
fun EnterEmailScreenTitleText(content: String, modifier: Modifier = Modifier) {
    Text(
        text = content,
        textAlign = TextAlign.Start,
        color = LocalColors.current.onSurface,
        style = PiaTypography.h1,
        fontSize = 28.sp,
        modifier = modifier,
    )
}

@Composable
fun SignUpTitleText(content: String, modifier: Modifier = Modifier) {
    Text(
        text = content,
        textAlign = TextAlign.Center,
        color = LocalColors.current.onSurface,
        style = PiaTypography.h1,
        fontSize = 45.sp,
        modifier = modifier,
    )
}

@Composable
fun SignupErrorTitleText(content: String, modifier: Modifier = Modifier) {
    Text(
        text = content,
        textAlign = TextAlign.Center,
        color = LocalColors.current.onSurface,
        style = PiaTypography.h1,
        fontSize = 28.sp,
        modifier = modifier,
    )
}

@Composable
fun SignupErrorDescriptionText(content: String, modifier: Modifier = Modifier) {
    Text(
        text = content,
        textAlign = TextAlign.Start,
        color = LocalColors.current.onSurface,
        style = PiaTypography.subtitle3,
        modifier = modifier,
    )
}

@Composable
fun SignupConsentTitleText(content: String, modifier: Modifier = Modifier) {
    Text(
        text = content,
        textAlign = TextAlign.Center,
        color = LocalColors.current.onSurface,
        style = PiaTypography.h1,
        fontSize = 28.sp,
        modifier = modifier,
    )
}

@Composable
fun SignupConsentDescriptionText(content: String, modifier: Modifier = Modifier) {
    Text(
        text = content,
        textAlign = TextAlign.Start,
        color = LocalColors.current.onSurface,
        style = PiaTypography.subtitle3,
        modifier = modifier,
    )
}

@Composable
fun SignupSuccessTitleText(content: String, modifier: Modifier = Modifier) {
    Text(
        text = content,
        textAlign = TextAlign.Center,
        color = LocalColors.current.onSurface,
        style = PiaTypography.h1,
        fontSize = 28.sp,
        modifier = modifier,
    )
}

@Composable
fun SignupSuccessDescriptionText(content: String, modifier: Modifier = Modifier) {
    Text(
        text = content,
        textAlign = TextAlign.Start,
        color = LocalColors.current.onSurface,
        style = PiaTypography.subtitle3,
        modifier = modifier,
    )
}

@Composable
fun OnboardingTitleText(content: String, modifier: Modifier = Modifier) {
    Text(
        text = content,
        color = LocalColors.current.onSurface,
        style = PiaTypography.h2,
        textAlign = TextAlign.Start,
        fontSize = 28.sp,
        modifier = modifier,
    )
}

@Composable
fun OnboardingDescriptionText(content: String, modifier: Modifier = Modifier) {
    Text(
        text = content,
        color = LocalColors.current.onSurface,
        style = PiaTypography.subtitle3,
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
        fontSize = 32.sp,
    )
}

@Composable
fun QuickConnectText(content: String, modifier: Modifier) {
    Text(
        text = content,
        textAlign = TextAlign.Center,
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
fun SelectedRegionTitleText(content: String) {
    Text(
        text = content,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = PiaTypography.caption2,
    )
}

@Composable
fun SelectedRegionServerText(content: String) {
    Text(
        text = content,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = PiaTypography.subtitle3,
    )
}

@Composable
fun RegionSelectionNameText(content: String, modifier: Modifier = Modifier) {
    Text(
        text = content,
        textAlign = TextAlign.Start,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = PiaTypography.body3,
        modifier = modifier,
    )
}

@Composable
fun RegionSelectionLatencyText(content: String, modifier: Modifier = Modifier) {
    Text(
        text = content,
        textAlign = TextAlign.Start,
        style = PiaTypography.caption1,
        modifier = modifier,
    )
}

@Composable
fun RegionSelectionGridSectionText(content: String, modifier: Modifier = Modifier) {
    Text(
        text = content,
        color = LocalColors.current.onSurfaceVariant,
        style = PiaTypography.subtitle1,
        modifier = modifier,
    )
}

@Composable
fun RegionSelectionDipText(content: String) {
    Text(
        text = content,
        style = PiaTypography.caption1,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .border(BorderStroke(1.dp, LocalColors.current.outline), RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
    )
}

@Composable
fun SettingsL2Text(content: String) {
    Text(
        text = content,
        style = PiaTypography.subtitle1,
    )
}

@Composable
fun SettingsL2TextDescription(content: String) {
    Text(
        text = content,
        style = PiaTypography.caption2,
    )
}

@Composable
fun DedicatedIpTitle(content: String, modifier: Modifier = Modifier) {
    Text(
        modifier = modifier,
        text = content,
        textAlign = TextAlign.Start,
        style = PiaTypography.h2,
        color = LocalColors.current.onSurfaceVariant,
    )
}

@Composable
fun DedicatedIpDescription(content: String, modifier: Modifier = Modifier) {
    Text(
        modifier = modifier,
        text = content,
        textAlign = TextAlign.Start,
        style = PiaTypography.body2,
        color = LocalColors.current.onSurfaceVariant,
    )
}

@Composable
fun DedicatedIpAddressDetailsTitle(content: String, modifier: Modifier = Modifier) {
    Text(
        modifier = modifier,
        text = content,
        textAlign = TextAlign.Start,
        style = PiaTypography.subtitle2,
        color = LocalColors.current.onSurfaceVariant,
    )
}

@Composable
fun DedicatedIpAddressDetailsSubtitle(content: String, modifier: Modifier = Modifier) {
    Text(
        modifier = modifier,
        text = content,
        textAlign = TextAlign.Start,
        style = PiaTypography.body2,
        color = LocalColors.current.onSurfaceVariant,
    )
}