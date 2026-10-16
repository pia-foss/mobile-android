package com.kape.automation.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.dp
import com.kape.automation.R
import com.kape.ui.mobile.elements.AdaptiveContent
import com.kape.ui.mobile.elements.AppColumn
import com.kape.ui.mobile.elements.PrimaryButton
import com.kape.ui.mobile.text.OnboardingDescriptionText
import com.kape.ui.mobile.text.OnboardingFooterText
import com.kape.ui.mobile.text.OnboardingTitleText

/**
 * Layout shared by the location permission screens: the explanation at the top and the
 * footer with the action at the bottom of the visible area, or beside each other when wide.
 */
@Composable
internal fun LocationPermissionContent(
    title: String,
    message: String,
    action: String,
    actionTestTag: String,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AdaptiveContent(
        modifier = modifier.semantics { testTagsAsResourceId = true },
        compact = { contentModifier ->
            // A minimum height of the visible area lets the spacer push the action to the bottom,
            // while taller content still scrolls
            val visibleHeight = maxHeight
            Column(
                modifier = contentModifier.verticalScroll(rememberScrollState()),
                horizontalAlignment = CenterHorizontally,
            ) {
                AppColumn(modifier = Modifier.heightIn(min = visibleHeight)) {
                    Header(title = title, isCompact = true)
                    Message(message)
                    Spacer(modifier = Modifier.weight(1f))
                    Action(action = action, testTag = actionTestTag, onClick = onActionClick)
                }
            }
        },
        wide = { contentModifier ->
            val visibleHeight = maxHeight
            Row(modifier = contentModifier) {
                Column(
                    modifier =
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState()),
                    horizontalAlignment = CenterHorizontally,
                ) {
                    AppColumn(modifier = Modifier.heightIn(min = visibleHeight), centerVertically = true) {
                        Header(title = title, isCompact = false)
                    }
                }
                Column(
                    modifier =
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState()),
                    horizontalAlignment = CenterHorizontally,
                ) {
                    AppColumn(modifier = Modifier.heightIn(min = visibleHeight)) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Message(message)
                        Spacer(modifier = Modifier.weight(1f))
                        Action(action = action, testTag = actionTestTag, onClick = onActionClick)
                    }
                }
            }
        },
    )
}

@Composable
private fun Header(
    title: String,
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
        painter = painterResource(id = R.drawable.ic_vpn_permission),
        contentDescription = null,
        modifier =
            Modifier
                .padding(if (isCompact) 40.dp else 16.dp)
                .height(if (isCompact) 140.dp else 80.dp)
                .fillMaxWidth(),
    )
    OnboardingTitleText(
        content = title,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
    )
}

@Composable
private fun Message(message: String) {
    OnboardingDescriptionText(
        content = message,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
    )
}

@Composable
private fun Action(
    action: String,
    testTag: String,
    onClick: () -> Unit,
) {
    OnboardingFooterText(
        content = stringResource(id = com.kape.ui.R.string.location_permission_footer),
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
    )
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