package com.kape.settings.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kape.appbar.view.mobile.AppBar
import com.kape.appbar.viewmodel.AppBarViewModel
import com.kape.settings.R
import com.kape.settings.data.VpnProtocols
import com.kape.settings.ui.elements.SettingsItem
import com.kape.settings.ui.vm.SettingsViewModel
import com.kape.ui.mobile.elements.Screen
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SettingsScreen() = Screen {
    val viewModel: SettingsViewModel = koinViewModel()
    val appBarViewModel: AppBarViewModel = koinViewModel<AppBarViewModel>().apply {
        appBarText(stringResource(id = com.kape.ui.R.string.settings))
    }
    val shouldShowObfuscation = viewModel.getSelectedProtocol() == VpnProtocols.OpenVPN

    BackHandler {
        viewModel.navigateUp()
    }

    Scaffold(
        topBar = {
            AppBar(
                viewModel = appBarViewModel,
                onLeftIconClick = { viewModel.navigateToConnection() },
            )
        },
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxWidth()
                .semantics {
                    testTagsAsResourceId = true
                },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(modifier = Modifier.widthIn(max = 520.dp)) {
                SettingsItem(
                    iconId = R.drawable.ic_settings_general,
                    titleId = com.kape.ui.R.string.general,
                    onClick = {
                        viewModel.navigateToGeneralSettings()
                    },
                )
                SettingsItem(
                    iconId = R.drawable.ic_settings_protocols,
                    titleId = com.kape.ui.R.string.protocols,
                    subtitle = viewModel.getSelectedProtocol().name,
                    onClick = {
                        viewModel.navigateToProtocolSettings()
                    },
                    testTag = ":SettingsScreen:Protocols",
                )
                SettingsItem(
                    iconId = R.drawable.ic_settings_network,
                    titleId = com.kape.ui.R.string.networks,
                    onClick = {
                        viewModel.navigateToNetworkSettings()
                    },
                )
                SettingsItem(
                    iconId = R.drawable.ic_settings_privacy,
                    titleId = com.kape.ui.R.string.privacy,
                    onClick = {
                        viewModel.navigateToPrivacySettings()
                    },
                )
                SettingsItem(
                    iconId = R.drawable.ic_settings_automation,
                    titleId = com.kape.ui.R.string.automation,
                    subtitle = stringResource(id = if (viewModel.isAutomationEnabled()) com.kape.ui.R.string.enabled else com.kape.ui.R.string.disabled),
                    onClick = {
                        viewModel.navigateToAutomationSettings()
                    },
                )
                if (shouldShowObfuscation) {
                    SettingsItem(
                        iconId = R.drawable.ic_settings_obfuscation,
                        titleId = com.kape.ui.R.string.obfuscation,
                        onClick = {
                            viewModel.navigateToObfuscationSettings()
                        },
                    )
                }
                SettingsItem(
                    iconId = R.drawable.ic_settings_help,
                    titleId = com.kape.ui.R.string.help,
                    onClick = {
                        viewModel.navigateToHelpSettings()
                    },
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewSettingsScreen() {
    SettingsScreen()
}