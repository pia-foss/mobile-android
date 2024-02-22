package com.kape.settings.ui.elements

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kape.ui.R
import com.kape.ui.elements.Separator
import com.kape.ui.text.SettingsL2Text
import com.kape.ui.text.SettingsL2TextDescription
import com.kape.ui.utils.LocalColors

@Composable
fun SettingsItem(
    @DrawableRes iconId: Int? = null,
    @StringRes titleId: Int,
    subtitle: String? = null,
    testTag: String? = null,
    onClick: (() -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .padding(vertical = 1.dp)
            .clickable(onClick = onClick ?: {}),
    ) {
        Row(
            modifier = Modifier
                .height(56.dp)
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            iconId?.let {
                Icon(
                    painter = painterResource(iconId),
                    contentDescription = stringResource(
                        id = R.string.icon,
                    ),
                    tint = Color.Unspecified,
                    modifier = Modifier.size(24.dp),
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
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