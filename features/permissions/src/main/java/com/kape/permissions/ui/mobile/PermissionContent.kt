package com.kape.permissions.ui.mobile

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.dp
import com.kape.ui.mobile.elements.AdaptiveSplitContent
import com.kape.ui.mobile.elements.PrimaryButton
import com.kape.ui.mobile.text.OnboardingDescriptionText
import com.kape.ui.mobile.text.OnboardingFooterText
import com.kape.ui.mobile.text.OnboardingTitleText

/**
 * Layout shared by the onboarding permission screens: the logo and [illustration], the
 * explanation, and the action at the bottom of the visible area; images and text side by
 * side when wide. Draws edge to edge, so it pads for the system bars and display cutout.
 */
@Composable
internal fun PermissionContent(
    @DrawableRes illustration: Int,
    title: String,
    description: String,
    action: String,
    actionTestTag: String,
    onActionClick: () -> Unit,
    footer: String? = null,
) {
    AdaptiveSplitContent(
        modifier =
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars.union(WindowInsets.displayCutout))
                .semantics {
                    testTagsAsResourceId = true
                },
        header = { isCompact -> PermissionImages(illustration = illustration, isCompact = isCompact) },
        body = {
            PermissionText(title, description, footer, modifier = Modifier.weight(1f))
            PermissionAction(action, actionTestTag, onActionClick)
        },
    )
}

@Composable
private fun PermissionImages(
    @DrawableRes illustration: Int,
    isCompact: Boolean,
) {
    Image(
        painter = painterResource(id = com.kape.ui.R.drawable.pia_medium),
        contentDescription = null,
        modifier =
            Modifier
                .padding(16.dp)
                .height(if (isCompact) 40.dp else 32.dp)
                .fillMaxWidth(),
    )
    Image(
        painter = painterResource(id = illustration),
        contentDescription = null,
        modifier =
            Modifier
                .padding(if (isCompact) 40.dp else 16.dp)
                .height(if (isCompact) 140.dp else 100.dp)
                .fillMaxWidth(),
    )
}

/**
 * Title, description and footer, read as one by screen readers. Given a weight, the spacer
 * inside pushes the footer (or, without one, the button) to the bottom.
 */
@Composable
private fun PermissionText(
    title: String,
    description: String,
    footer: String?,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.semantics(mergeDescendants = true) { }) {
        OnboardingTitleText(
            content = title,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
        )
        OnboardingDescriptionText(
            content = description,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
        )
        Spacer(modifier = Modifier.weight(1f))
        footer?.let {
            OnboardingFooterText(
                content = it,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
            )
        }
    }
}

@Composable
private fun PermissionAction(
    action: String,
    testTag: String,
    onClick: () -> Unit,
) {
    PrimaryButton(
        text = action,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 4.dp, bottom = 36.dp, end = 16.dp)
                .testTag(testTag),
        onClick = onClick,
    )
}