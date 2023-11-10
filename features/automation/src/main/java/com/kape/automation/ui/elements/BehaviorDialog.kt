package com.kape.automation.ui.elements

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kape.networkmanagement.R
import com.kape.ui.text.DialogActionText
import com.kape.ui.text.DialogTitleText
import com.kape.ui.utils.LocalColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BehaviorDialog(
    status: String,
    showRemoveOption: Boolean,
    showDialog: MutableState<Boolean>,
    onItemSelected: (selected: String) -> Unit,
) {
    AlertDialog(
        onDismissRequest = { showDialog.value = false },
        modifier = Modifier.background(LocalColors.current.surfaceVariant),
    ) {
        val radioOptions = mutableListOf(
            stringResource(id = R.string.nmt_connect),
            stringResource(id = R.string.nmt_disconnect),
            stringResource(id = R.string.nmt_retain),
        )

        if (showRemoveOption) {
            radioOptions.add(stringResource(id = com.kape.automation.R.string.nmt_remove_rule))
        }

        val selectedOption = remember {
                mutableStateOf(
                    radioOptions[
                        radioOptions.indexOf(
                            status,
                        ),
                    ],
                )
            }

        Column(
            Modifier.fillMaxWidth(),
        ) {
            DialogTitleText(
                content = stringResource(id = com.kape.automation.R.string.dialog_title),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        LocalColors.current.surface,
                    )
                    .padding(16.dp),
            )
            radioOptions.forEach { text ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = (text == selectedOption.value),
                            onClick = {
                                selectedOption.value = text
                            },
                        )
                        .padding(vertical = 4.dp),
                ) {
                    RadioButton(
                        selected = (text == selectedOption.value),
                        onClick = {
                            selectedOption.value = text
                        },
                    )
                    Text(
                        text = text,
                        modifier = Modifier.align(Alignment.CenterVertically),
                    )
                }
            }

            Row(
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(16.dp),
            ) {
                DialogActionText(
                    content = stringResource(id = android.R.string.cancel),
                    modifier = Modifier.clickable {
                        showDialog.value = false
                    },
                )
                Spacer(modifier = Modifier.width(16.dp))
                DialogActionText(
                    content = stringResource(id = android.R.string.ok),
                    modifier = Modifier.clickable {
                        onItemSelected(selectedOption.value)
                        showDialog.value = false
                    },
                )
            }
        }
    }
}