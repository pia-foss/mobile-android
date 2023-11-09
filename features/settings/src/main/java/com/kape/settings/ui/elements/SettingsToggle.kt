package com.kape.settings.ui.elements

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kape.ui.elements.MenuSeparator
import com.kape.ui.elements.Toggle
import com.kape.ui.text.SettingsL2Text
import com.kape.ui.text.SettingsL2TextDescription

@Composable
fun SettingsToggle(
    @StringRes titleId: Int,
    @StringRes subtitleId: Int? = null,
    @DrawableRes iconId: Int? = null,
    enabled: Boolean = false,
    stateEnabled: MutableState<Boolean> = mutableStateOf(enabled),
    toggle: (checked: Boolean) -> Unit,
) {
    val isChecked = remember { stateEnabled }
    Column(
        modifier = Modifier
            .defaultMinSize(minHeight = 56.dp),
    ) {
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
                SettingsL2Text(content = stringResource(id = titleId))

                subtitleId?.let {
                    SettingsL2TextDescription(content = stringResource(id = it))
                }
            }
            Toggle(isOn = isChecked.value) {
                isChecked.value = it
                toggle(it)
            }
        }
        MenuSeparator()
    }
}