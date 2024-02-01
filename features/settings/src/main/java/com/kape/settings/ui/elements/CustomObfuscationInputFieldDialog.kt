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
import com.kape.settings.R
import com.kape.settings.data.CustomObfuscation
import com.kape.ui.text.Input

@Composable
fun CustomObfuscationInputFieldDialog(
    @StringRes titleId: Int,
    current: CustomObfuscation?,
    onConfirm: (custom: CustomObfuscation) -> Unit,
    onDismiss: () -> Unit,
    isNumericIpAddress: (ipAddress: String) -> Boolean,
    isPortValid: (port: String) -> Boolean,
) {
    val host = remember { mutableStateOf(current?.host ?: "") }
    val port = remember { mutableStateOf(current?.port ?: "") }
    val key = remember { mutableStateOf(current?.key ?: "") }
    val cipher = remember { mutableStateOf(current?.cipher ?: "") }

    val hostErrorMessage = stringResource(id = R.string.obfuscation_shadowsocks_invalid_host)
    val portErrorMessage = stringResource(id = R.string.obfuscation_shadowsocks_invalid_port)

    fun showHostErrorIfNeeded(): String? {
        return if (host.value.isEmpty() || isNumericIpAddress(host.value)) {
            null
        } else {
            hostErrorMessage
        }
    }

    fun showPortErrorIfNeeded(): String? {
        return if (port.value.isEmpty() || isPortValid(port.value)) {
            null
        } else {
            portErrorMessage
        }
    }

    fun onConfirmClicked() {
        onConfirm(
            CustomObfuscation(
                host = host.value,
                port = port.value,
                key = key.value,
                cipher = cipher.value,
            ),
        )
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
                        host.value = ""
                        port.value = ""
                        key.value = ""
                        cipher.value = ""
                    },
                    modifier = Modifier.testTag(":OptionsDialog:Clear"),
                ) {
                    Text(text = stringResource(id = R.string.clear))
                }
                TextButton(
                    onClick = {
                        onConfirmClicked()
                    },
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
                            label = stringResource(id = R.string.obfuscation_shadowsocks_host),
                            maskInput = false,
                            keyboard = KeyboardType.Decimal,
                            content = host,
                            errorMessage = showHostErrorIfNeeded(),
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
                            label = stringResource(id = R.string.obfuscation_shadowsocks_port),
                            maskInput = false,
                            keyboard = KeyboardType.Decimal,
                            content = port,
                            errorMessage = showPortErrorIfNeeded(),
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
                            label = stringResource(id = R.string.obfuscation_shadowsocks_key),
                            maskInput = false,
                            keyboard = KeyboardType.Text,
                            content = key,
                            errorMessage = null,
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
                            label = stringResource(id = R.string.obfuscation_shadowsocks_cipher),
                            maskInput = false,
                            keyboard = KeyboardType.Text,
                            content = cipher,
                            errorMessage = null,
                        )
                    }
                }
            }
        },
    )
}