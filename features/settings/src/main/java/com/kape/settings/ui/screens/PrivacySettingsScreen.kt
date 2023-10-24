package com.kape.settings.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.kape.appbar.view.AppBar
import com.kape.appbar.viewmodel.AppBarViewModel
import com.kape.settings.R
import com.kape.settings.ui.elements.SettingsItem
import com.kape.settings.ui.elements.SettingsToggle
import com.kape.settings.ui.vm.SettingsViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun PrivacySettingsScreen() {
    val viewModel: SettingsViewModel = koinViewModel()
    val appBarViewModel: AppBarViewModel = koinViewModel<AppBarViewModel>().apply {
        appBarText(stringResource(id = R.string.privacy))
    }
    val showWarning = remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            AppBar(
                viewModel = appBarViewModel,
            ) {
                viewModel.exitPrivacySettings()
            }
        },
    ) {
        Column(
            modifier = Modifier
                .padding(it),
        ) {
            SettingsItem(
                titleId = R.string.privacy_kill_switch_title,
                subtitle = stringResource(id = R.string.privacy_kill_switch_description),
                onClick = {
                    viewModel.navigateToKillSwitch()
                },
            )
            SettingsToggle(
                titleId = R.string.mace_title,
                subtitleId = R.string.mace_description,
                enabled = viewModel.maceEnabled.value,
                toggle = {
                    if (viewModel.getCustomDns().isInUse() && !viewModel.maceEnabled.value) {
                        showWarning.value = true
                    } else {
                        viewModel.toggleMace(it)
                    }
                },
            )
            if (showWarning.value) {
                WarningDialog {
                    viewModel.toggleMace(true)
                    showWarning.value = false
                }
            }

        }
    }
}

@Composable
fun WarningDialog(onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onConfirm,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = stringResource(id = R.string.ok))
            }
        },
        title = {
            Text(
                text = stringResource(id = R.string.custom_dns),
                style = MaterialTheme.typography.titleMedium,
            )
        },
        text = {
            Text(text = stringResource(id = R.string.custom_dns_mace_warning))
        },
    )
}