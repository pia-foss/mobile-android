@file:OptIn(ExperimentalComposeUiApi::class)

package com.kape.ui.tiles

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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.dp
import com.kape.ui.R

@Composable
fun Dialog(
    title: String,
    text: String,
    onConfirmButtonText: String,
    onDismissButtonText: String? = null,
    onConfirm: () -> Unit,
    onDismiss: (() -> Unit)? = null,
) {
    val onDismissRequest = onDismiss?.let { onDismiss } ?: onConfirm
    AlertDialog(
        modifier = Modifier.semantics { testTagsAsResourceId = true },
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                modifier = Modifier.testTag(
                    ":SideMenu:ConfirmButton",
                ),
            ) {
                Text(text = onConfirmButtonText)
            }
        },
        dismissButton = {
            if (onDismiss == null) {
                return@AlertDialog
            }
            if (onDismissButtonText == null) {
                return@AlertDialog
            }

            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag(
                    ":SideMenu:DismissButton",
                ),
            ) {
                Text(text = onDismissButtonText)
            }
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )
        },
        text = {
            Column(
                Modifier.fillMaxWidth(),
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = text)
                }
            }
        },
    )
}

@Composable
fun LogoutDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        modifier = Modifier.semantics { testTagsAsResourceId = true },
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                modifier = Modifier.testTag(
                    ":SideMenu:ConfirmButton",
                ),
            ) {
                Text(text = stringResource(id = R.string.drawer_item_title_logout))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag(
                    ":SideMenu:DismissButton",
                ),
            ) {
                Text(text = stringResource(id = R.string.cancel))
            }
        },
        title = {
            Text(
                text = stringResource(id = R.string.logout_confirmation),
                style = MaterialTheme.typography.titleMedium,
            )
        },
        text = {
            Column(
                Modifier.fillMaxWidth(),
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = stringResource(id = R.string.logout_confirmation_text))
                }
            }
        },
    )
}