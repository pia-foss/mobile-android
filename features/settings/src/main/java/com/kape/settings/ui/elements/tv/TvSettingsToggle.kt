@file:OptIn(ExperimentalTvMaterial3Api::class)

package com.kape.settings.ui.elements.tv

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Switch
import androidx.tv.material3.SwitchDefaults
import com.kape.ui.tv.text.SettingsL2Text
import com.kape.ui.tv.text.SettingsL2TextDescription
import com.kape.ui.utils.LocalColors

@Composable
fun TvSettingsToggle(
    modifier: Modifier = Modifier,
    @StringRes titleId: Int,
    @StringRes subtitleId: Int? = null,
    enabled: Boolean = false,
    stateEnabled: MutableState<Boolean> = mutableStateOf(enabled),
    toggle: (checked: Boolean) -> Unit,
) {
    val isChecked = remember { stateEnabled }
    Button(
        modifier = modifier.fillMaxWidth(),
        shape = ButtonDefaults.shape(
            shape = RoundedCornerShape(12.dp),
        ),
        colors = ButtonDefaults.colors(
            containerColor = LocalColors.current.background,
            contentColor = LocalColors.current.onSurfaceVariant,
            focusedContainerColor = LocalColors.current.primary,
            focusedContentColor = LocalColors.current.onPrimary,
        ),
        onClick = {
            isChecked.value = !isChecked.value
            toggle(isChecked.value)
        },
    ) {
        Column(modifier = Modifier.weight(1f)) {
            SettingsL2Text(content = stringResource(id = titleId))
            subtitleId?.let {
                SettingsL2TextDescription(content = stringResource(id = it))
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Switch(
            checked = isChecked.value,
            colors = SwitchDefaults.colors(
                uncheckedBorderColor = LocalColors.current.onSurfaceVariant,
                checkedBorderColor = Color.Unspecified,
                uncheckedTrackColor = LocalColors.current.onPrimary,
                checkedTrackColor = LocalColors.current.primary,
                uncheckedIconColor = LocalColors.current.onSurfaceVariant,
                checkedIconColor = LocalColors.current.onPrimary,
                uncheckedThumbColor = LocalColors.current.onSurfaceVariant,
            ),
            onCheckedChange = null,
        )
    }
}