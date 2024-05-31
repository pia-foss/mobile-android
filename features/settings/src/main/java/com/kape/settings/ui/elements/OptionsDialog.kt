package com.kape.settings.ui.elements

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.dp
import com.kape.settings.utils.ButtonProperty
import com.kape.settings.utils.ButtonType
import com.kape.ui.R

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun <T> OptionsDialog(
    @StringRes titleId: Int,
    options: Map<T, String>,
    buttons: Map<ButtonType, ButtonProperty>,
    onDismiss: () -> Unit,
    onConfirm: (selection: T) -> Unit,
    onNeutral: () -> Unit = {},
    selection: T,
) {
    val selected = remember { mutableStateOf(selection) }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Row {
                buttons[ButtonType.Neutral]?.let {
                    it.label?.let { label ->
                        TextButton(
                            onClick = onNeutral,
                            modifier = Modifier.testTag(":OptionsDialog:$label"),
                        ) {
                            Text(text = label)
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }
                buttons[ButtonType.Negative]?.label?.let {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag(":OptionsDialog:$it"),
                    ) {
                        Text(text = it)
                    }
                }
                TextButton(
                    onClick = { onConfirm(selected.value) },
                    modifier = Modifier.testTag(":OptionsDialog:Ok"),
                ) {
                    Text(
                        text = buttons[ButtonType.Positive]?.label
                            ?: stringResource(id = R.string.ok),
                    )
                }
            }
        },
        modifier = Modifier
            .semantics {
                testTagsAsResourceId = true
            },
        title = {
            Text(text = stringResource(id = titleId), style = MaterialTheme.typography.titleMedium)
        },
        text = {
            Column(
                Modifier.fillMaxWidth().selectableGroup(),
            ) {
                options.forEach {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (it.key == selected.value),
                                onClick = {
                                    selected.value = it.key
                                },
                            )
                            .padding(vertical = 8.dp)
                            .semantics {
                                testTagsAsResourceId = true
                            },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = (it.key == selected.value),
                            onClick = null,
                        )
                        Text(
                            text = it.value,
                            Modifier
                                .padding(horizontal = 8.dp)
                                .testTag(":OptionsDialog:${it.value}"),
                        )
                    }
                }
            }
        },
    )
}