package com.kape.settings.ui.elements

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.kape.settings.data.ObfuscationOptions
import com.kape.settings.utils.ButtonProperty
import com.kape.settings.utils.ButtonType
import com.kape.ui.R

@Composable
fun ObfuscationSelectionDialog(
    options: Map<ObfuscationOptions, String>,
    selection: ObfuscationOptions,
    onConfirm: (option: ObfuscationOptions) -> Unit,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
) {
    OptionsDialog(
        R.string.obfuscation_shadowsocks_title,
        options = options,
        buttons = mapOf(
            ButtonType.Positive to ButtonProperty(
                ButtonType.Positive,
                stringResource(id = R.string.save).uppercase(),
            ),
            ButtonType.Negative to ButtonProperty(
                ButtonType.Negative,
                stringResource(id = R.string.cancel).uppercase(),
            ),
            ButtonType.Neutral to ButtonProperty(
                ButtonType.Neutral,
                stringResource(id = R.string.edit).uppercase(),
            ),
        ),
        onDismiss = onDismiss,
        onConfirm = onConfirm,
        onNeutral = onEdit,
        selection = selection,
    )
}