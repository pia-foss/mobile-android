package com.kape.settings.ui.elements.mobile

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.kape.ui.R
import com.kape.ui.mobile.elements.Separator
import com.kape.ui.mobile.text.SettingsL2Text
import com.kape.ui.mobile.text.SettingsL2TextDescription
import com.kape.ui.utils.LocalColors

@Composable
fun SettingsItem(
    @DrawableRes iconId: Int? = null,
    @StringRes titleId: Int,
    subtitle: String? = null,
    testTag: String? = null,
    onClick: (() -> Unit)? = null,
) {
    // Screen readers interpret the clickable modifier as an interactive element. By removing
    // this modifier, the screen reader will no longer wrongly suggest the user to "tap twice to activate"
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }
    Column(
        modifier = Modifier
            .padding(vertical = 1.dp)
            .then(clickableModifier)
            .semantics(mergeDescendants = true) {},
    ) {
        Row(
            modifier = Modifier
                .height(56.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            iconId?.let {
                Icon(
                    painter = painterResource(iconId),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(modifier = Modifier.width(16.dp))
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .testTag(testTag.toString()),
            ) {
                SettingsL2Text(
                    content = stringResource(id = titleId),
                )

                subtitle?.let {
                    SettingsL2TextDescription(
                        content = it,
                    )
                }
            }
            onClick?.let {
                Spacer(modifier = Modifier.width(16.dp))
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow),
                    contentDescription = null,
                    tint = LocalColors.current.onBackground,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
        Separator()
    }
}