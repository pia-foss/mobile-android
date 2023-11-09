package com.kape.settings.ui.elements

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kape.settings.R
import com.kape.ui.elements.InputField
import com.kape.ui.elements.InputFieldProperties
import com.kape.ui.theme.Space
import com.kape.ui.utils.LocalColors

@Composable
fun InputFieldDialog(
    @StringRes titleId: Int,
    inputFieldProperties: List<InputFieldProperties>,
    onClear: () -> Unit,
    onConfirm: () -> Unit,
    footnote: String? = null,
) {
    AlertDialog(
        onDismissRequest = onConfirm,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = stringResource(id = R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onClear) {
                Text(text = stringResource(id = R.string.clear))
            }
        },
        title = {
            Text(text = stringResource(id = titleId), style = MaterialTheme.typography.titleMedium)
        },
        text = {
            Column(
                Modifier.fillMaxWidth(),
            ) {
                inputFieldProperties.forEach { inputFieldProperty ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        InputField(
                            modifier = Modifier.padding(Space.MEDIUM, Space.SMALL),
                            properties = inputFieldProperty,
                        )
                    }
                }
                footnote?.let {
                    Text(
                        text = it,
                        color = LocalColors.current.onSurface,
                    )
                }
            }
        },
    )
}