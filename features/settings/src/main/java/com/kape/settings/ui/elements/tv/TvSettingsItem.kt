@file:OptIn(ExperimentalTvMaterial3Api::class)

package com.kape.settings.ui.elements.tv

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import com.kape.ui.tv.text.SettingsL2Text
import com.kape.ui.tv.text.SettingsL2TextDescription
import com.kape.ui.utils.LocalColors

@Composable
fun TvSettingsItem(
    modifier: Modifier = Modifier,
    @StringRes titleId: Int,
    subtitle: String?,
    onClick: (() -> Unit),
) {
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
        onClick = onClick,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            SettingsL2Text(content = stringResource(id = titleId))
            subtitle?.let {
                SettingsL2TextDescription(content = it)
            }
        }
    }
}