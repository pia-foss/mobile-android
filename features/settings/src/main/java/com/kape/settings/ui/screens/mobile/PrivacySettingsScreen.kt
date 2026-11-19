package com.kape.settings.ui.screens.mobile

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kape.appbar.view.mobile.AppBar
import com.kape.appbar.viewmodel.AppBarViewModel
import com.kape.settings.data.DnsOptions
import com.kape.settings.ui.elements.ReconnectPromptDialog
import com.kape.settings.ui.elements.rememberMockLocationToggleState
import com.kape.settings.ui.vm.MockLocationViewModel
import com.kape.settings.ui.vm.PrivacySettingsViewModel
import com.kape.ui.R
import com.kape.ui.Screen
import com.kape.ui.mobile.elements.SettingsItem
import com.kape.ui.mobile.elements.SettingsToggle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PrivacySettingsScreen() =
    Screen {
        val viewModel: PrivacySettingsViewModel = koinViewModel()
        val appBarViewModel: AppBarViewModel =
            koinViewModel<AppBarViewModel>().apply {
                appBarText(stringResource(id = R.string.privacy))
            }
        val showWarning = rememberSaveable { mutableStateOf(false) }
        val maceEnabled by viewModel.maceEnabled.collectAsStateWithLifecycle()
        val selectedDnsOption by viewModel.selectedDnsOption.collectAsStateWithLifecycle()
        val mockLocationToggleState = rememberMockLocationToggleState(koinViewModel<MockLocationViewModel>())

        Scaffold(
            topBar = {
                AppBar(viewModel = appBarViewModel)
            },
        ) {
            Column(
                modifier =
                    Modifier
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
                        enabled = maceEnabled,
                        toggle = {
                            if (selectedDnsOption != DnsOptions.PIA && !maceEnabled) {
                                showWarning.value = true
                            }
                            viewModel.toggleMace(it)
                            viewModel.reconnectPrompt.onSettingChanged()
                        },
                    )
                    if (viewModel.isMockLocationAvailable) {
                        SettingsToggle(
                            titleId = R.string.mock_location_title,
                            subtitleId = R.string.mock_location_description,
                            enabled = mockLocationToggleState.enabled,
                            toggle = mockLocationToggleState.toggle,
                        )
                    }
                    ReconnectPromptDialog(viewModel.reconnectPrompt)
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