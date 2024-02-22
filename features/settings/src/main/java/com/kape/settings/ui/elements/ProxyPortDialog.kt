package com.kape.settings.ui.elements

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
import com.kape.ui.R
import com.kape.ui.text.Input

@Composable
fun ProxyPortDialog(
    currentPort: String,
    onConfirm: (port: String) -> Unit,
    onDefault: () -> Unit,
    onDismiss: () -> Unit,
) {
    val port = remember { mutableStateOf(currentPort) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Row {
                TextButton(
                    onClick = onDefault,
                    modifier = Modifier.testTag(":OptionsDialog:Default"),
                ) {
                    Text(text = stringResource(id = R.string.default_option))
                }
                Spacer(modifier = Modifier.weight(1f))
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag(":OptionsDialog:Cancel"),
                ) {
                    Text(text = stringResource(id = R.string.cancel))
                }
                TextButton(
                    onClick = { onConfirm(port.value) },
                    modifier = Modifier.testTag(":OptionsDialog:Save"),
                ) {
                    Text(
                        text = stringResource(id = R.string.save),
                    )
                }
            }
        },
        title = {
            Text(
                text = stringResource(id = R.string.proxy_port),
                style = MaterialTheme.typography.titleMedium,
            )
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
                            modifier = Modifier.padding(16.dp, 8.dp),
                            maskInput = false,
                            keyboard = KeyboardType.Decimal,
                            content = port,
                        )
                    }
                }
            }
        },
    )
}