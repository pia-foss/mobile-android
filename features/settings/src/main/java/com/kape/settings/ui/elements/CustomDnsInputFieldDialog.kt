package com.kape.settings.ui.elements

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kape.settings.data.CustomDns
import com.kape.ui.R
import com.kape.ui.mobile.text.Input
import com.kape.ui.utils.LocalColors

@Composable
fun CustomDnsInputFieldDialog(
    @StringRes titleId: Int,
    current: CustomDns,
    onConfirm: (custom: CustomDns) -> Unit,
    onDismiss: () -> Unit,
    footnote: String? = null,
    isDnsNumeric: (ipAddress: String) -> Boolean,
) {
    val primaryDns = remember { mutableStateOf(current.primaryDns) }
    val secondaryDns = remember { mutableStateOf(current.secondaryDns) }

    fun showPrimaryDnsErrorIfNeeded(error: String): String? {
        return if (primaryDns.value.isEmpty()) {
            if (secondaryDns.value.isEmpty()) {
                null
            } else {
                error
            }
        } else {
            if (isDnsNumeric(primaryDns.value)) {
                null
            } else {
                error
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Row {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag(":OptionsDialog:Cancel"),
                ) {
                    Text(text = stringResource(id = R.string.cancel))
                }
                Spacer(modifier = Modifier.weight(1f))
                TextButton(
                    onClick = {
                        primaryDns.value = ""
                        secondaryDns.value = ""
                    },
                    modifier = Modifier.testTag(":OptionsDialog:Clear"),
                ) {
                    Text(text = stringResource(id = R.string.clear))
                }
                TextButton(
                    onClick = { onConfirm(CustomDns(primaryDns.value, secondaryDns.value)) },
                    modifier = Modifier.testTag(":OptionsDialog:Ok"),
                ) {
                    Text(
                        text = stringResource(id = R.string.save),
                    )
                }
            }
        },
        title = {
            Text(text = stringResource(id = titleId), style = MaterialTheme.typography.titleMedium)
        },
        text = {
            Column(
                Modifier.fillMaxWidth(),
            ) {
                Column {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Input(
                            modifier = Modifier.padding(24.dp, 8.dp),
                            label = stringResource(id = R.string.network_dns_selection_custom_primary),
                            maskInput = false,
                            keyboard = KeyboardType.Decimal,
                            content = primaryDns,
                            errorMessage = showPrimaryDnsErrorIfNeeded(stringResource(id = R.string.network_dns_selection_custom_invalid)),
                        )
                    }
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Input(
                            modifier = Modifier.padding(24.dp, 8.dp),
                            label = stringResource(id = R.string.network_dns_selection_custom_secondary),
                            maskInput = false,
                            keyboard = KeyboardType.Decimal,
                            content = secondaryDns,
                            errorMessage = if (
                                secondaryDns.value.isEmpty() ||
                                isDnsNumeric(secondaryDns.value)
                            ) {
                                null
                            } else {
                                stringResource(id = R.string.network_dns_selection_custom_invalid)
                            },
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