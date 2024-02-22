package com.kape.settings.ui.elements

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.kape.ui.R

@Composable
fun ReconnectDialog(
    onReconnect: () -> Unit,
    onLater: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onReconnect,
        confirmButton = {
            TextButton(onClick = onReconnect) {
                Text(text = stringResource(id = R.string.reconnect))
            }
        },
        dismissButton = {
            TextButton(onClick = onLater) {
                Text(text = stringResource(id = R.string.later))
            }
        },
        title = {
            Text(text = stringResource(id = R.string.settings), style = MaterialTheme.typography.titleMedium)
        },
        text = {
            Text(text = stringResource(id = R.string.reconnect_apply_changes), style = MaterialTheme.typography.bodyMedium)
        },
    )
}