package com.kape.settings.ui.elements

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kape.ui.theme.FontSize
import com.kape.ui.utils.LocalColors

@Composable
fun SettingsToggle(
    @StringRes titleId: Int,
    @StringRes subtitleId: Int? = null,
    @DrawableRes iconId: Int? = null,
    enabled: Boolean,
    toggle: (checked: Boolean) -> Unit,
) {
    val isChecked = remember { mutableStateOf(enabled) }
    Row(
        modifier = Modifier
            .defaultMinSize(minHeight = 56.dp)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {

        iconId?.let {
            Icon(
                painter = painterResource(id = it),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(24.dp),
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
        ) {
            Text(
                text = stringResource(id = titleId),
                fontSize = FontSize.Normal,
                color = LocalColors.current.onSurface,
            )

            subtitleId?.let {
                Text(
                    text = stringResource(id = it),
                    fontSize = FontSize.Normal,
                    color = LocalColors.current.onSurfaceVariant,
                )
            }
        }
        Switch(
            checked = isChecked.value,
            onCheckedChange = {
                isChecked.value = it
                toggle(it)
            },
        )
    }
}