package com.kape.settings.ui.elements

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kape.settings.R

@Composable
fun OptionsDialog(
    @StringRes titleId: Int,
    options: List<String>,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    onOptionSelected: ((String) -> Unit)? = null,
    selection: MutableState<String>,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = stringResource(id = R.string.ok))
            }
        },
        title = {
            Text(text = stringResource(id = titleId), style = MaterialTheme.typography.titleMedium)
        },
        text = {
            Column(
                Modifier.fillMaxWidth(),
            ) {
                options.forEach { text ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (text == selection.value),
                                onClick = {
                                    selection.value = text
                                    onOptionSelected?.let {
                                        it(text)
                                    }
                                },
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = (text == selection.value),
                            onClick = null,
                        )
                        Text(
                            text = text,
                            Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
            }
        },
    )
}