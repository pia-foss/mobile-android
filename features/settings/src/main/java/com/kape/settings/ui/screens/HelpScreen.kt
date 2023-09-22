package com.kape.settings.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kape.appbar.view.NavigationAppBar
import com.kape.appbar.viewmodel.AppBarViewModel
import com.kape.settings.R
import com.kape.settings.ui.elements.SettingsItem
import com.kape.settings.ui.elements.SettingsToggle
import com.kape.settings.ui.vm.SettingsViewModel
import com.kape.ui.theme.FontSize
import com.kape.ui.utils.LocalColors
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen() {
    val viewModel: SettingsViewModel = koinViewModel()
    val appBarViewModel: AppBarViewModel = koinViewModel<AppBarViewModel>().apply {
        appBarText(stringResource(id = R.string.help))
    }
    val context = LocalContext.current
    val showDialog = remember { mutableStateOf(false) }
    val showToast = remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            NavigationAppBar(
                viewModel = appBarViewModel,
                onLeftButtonClick = {
                    viewModel.navigateUp()
                },
            )
        },
    ) {
        Column(
            modifier = Modifier
                .padding(it),
        ) {
            with(viewModel.requestId.value) {
                when {
                    this == null -> {
                        showDialog.value = false
                        showToast.value = false
                    }

                    this.isEmpty() -> {
                        showToast.value = true
                    }

                    this.isNotEmpty() -> {
                        showDialog.value = true
                    }
                }
            }

            val appUrl = stringResource(id = R.string.app_url)
            SettingsItem(
                titleId = R.string.help_version_title,
                subtitle = viewModel.version,
                onClick = {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse(appUrl)
                        },
                    )
                },
            )
            Divider(
                color = LocalColors.current.outline,
                modifier = Modifier.padding(vertical = 8.dp),
            )
            SettingsItem(
                titleId = R.string.help_view_debug_log_title,
                subtitle = stringResource(id = R.string.help_view_debug_log_description),
                onClick = {
                    viewModel.navigateToDebugLogs()
                },
            )
            Divider(
                color = LocalColors.current.outline,
                modifier = Modifier.padding(vertical = 8.dp),
            )
            SettingsItem(
                titleId = R.string.help_send_log_title,
                onClick = {
                    viewModel.sendLogs()
                },
            )
            Divider(
                color = LocalColors.current.outline,
                modifier = Modifier.padding(vertical = 8.dp),
            )
            SettingsToggle(
                titleId = R.string.help_improve_pia_title,
                subtitleId = R.string.help_improve_pia_description,
                enabled = viewModel.improvePiaEnabled.value,
                toggle = {
                    viewModel.toggleImprovePia(it)
                },
            )
            if (viewModel.improvePiaEnabled.value) {
                Divider(
                    color = LocalColors.current.outline,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
                SettingsItem(
                    titleId = R.string.help_view_shared_data_title,
                    onClick = {
                        viewModel.navigateToConnectionStats()
                    },
                )
            }
            Divider(
                color = LocalColors.current.outline,
                modifier = Modifier.padding(vertical = 8.dp),
            )

            if (showDialog.value) {
                SuccessDialog(
                    requestId = viewModel.requestId.value ?: "",
                    showDialog = showDialog,
                ) { viewModel.resetRequestId() }
            }

            if (showToast.value) {
                Toast.makeText(context, R.string.failure_sending_log, Toast.LENGTH_LONG)
                    .show()
                showToast.value = false
            }
        }
    }
}

@Composable
fun SuccessDialog(requestId: String, showDialog: MutableState<Boolean>, reset: () -> Unit) {
    AlertDialog(
        onDismissRequest = {
            showDialog.value = false
            reset()
        },
        title = {
            Text(
                text = stringResource(id = R.string.log_send_done_title),
                fontSize = FontSize.Title,
            )
        },
        text = {
            Text(
                text = String.format(
                    stringResource(id = R.string.log_send_done_msg),
                    requestId,
                ),
                fontSize = FontSize.Normal,
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    showDialog.value = false
                    reset()
                },
            ) {
                Text(
                    text = stringResource(id = R.string.ok),
                    fontSize = FontSize.Normal,
                    color = LocalColors.current.primary,
                )
            }
        },
    )
}