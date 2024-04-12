package com.kape.settings.ui.screens.mobile

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.kape.appbar.view.mobile.AppBar
import com.kape.appbar.viewmodel.AppBarViewModel
import com.kape.settings.data.DnsOptions
import com.kape.settings.ui.elements.ReconnectDialog
import com.kape.settings.ui.elements.mobile.SettingsItem
import com.kape.settings.ui.elements.mobile.SettingsToggle
import com.kape.settings.ui.vm.SettingsViewModel
import com.kape.ui.R
import com.kape.ui.mobile.elements.Screen
import org.koin.androidx.compose.koinViewModel

@Composable
fun PrivacySettingsScreen() = Screen {
    val viewModel: SettingsViewModel = koinViewModel()
    val appBarViewModel: AppBarViewModel = koinViewModel<AppBarViewModel>().apply {
        appBarText(stringResource(id = R.string.privacy))
    }
    val showWarning = remember { mutableStateOf(false) }

    BackHandler {
        viewModel.navigateUp()
    }

    Scaffold(
        topBar = {
            AppBar(
                viewModel = appBarViewModel,
                onLeftIconClick = { viewModel.navigateUp() },
            )
        },
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxWidth()
                .semantics {},
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(modifier = Modifier.widthIn(max = 520.dp)) {
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
                        // MACE requires using PIA DNS
                        if (viewModel.getSelectedDnsOption() != DnsOptions.PIA && !viewModel.maceEnabled.value) {
                            showWarning.value = true
                        }
                        viewModel.toggleMace(it)
                        viewModel.showReconnectDialogIfVpnConnected()
                    },
                )
                if (viewModel.reconnectDialogVisible.value) {
                    ReconnectDialog(
                        onReconnect = {
                            viewModel.reconnect()
                            viewModel.reconnectDialogVisible.value = false
                        },
                        onLater = {
                            viewModel.reconnectDialogVisible.value = false
                        },
                    )
                }
                if (showWarning.value) {
                    WarningDialog(
                        onConfirm = {
                            viewModel.setSelectedDnsOption(DnsOptions.PIA)
                            showWarning.value = false
                        },
                    )
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