package com.kape.settings.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.kape.appbar.view.NavigationAppBar
import com.kape.appbar.viewmodel.AppBarViewModel
import com.kape.settings.R
import com.kape.settings.ui.elements.SettingsItem
import com.kape.settings.ui.vm.SettingsViewModel
import com.kape.ui.utils.LocalColors
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val viewModel: SettingsViewModel = koinViewModel()
    val appBarViewModel: AppBarViewModel = koinViewModel<AppBarViewModel>().apply {
        appBarText(stringResource(id = R.string.settings))
    }

    Scaffold(
        topBar = {
            NavigationAppBar(
                viewModel = appBarViewModel,
                onLeftButtonClick = {
                    viewModel.navigateToConnection()
                },
            )
        },
    ) {
        Column(
            modifier = Modifier
                .padding(it),
        ) {
            SettingsItem(
                iconId = R.drawable.ic_setting_general,
                titleId = R.string.general,
                onClick = {
                    viewModel.navigateToGeneralSettings()
                },
            )
            Divider(color = LocalColors.current.outline)
            SettingsItem(
                iconId = R.drawable.ic_setting_protocols,
                titleId = R.string.protocols,
                subtitle = viewModel.getSelectedProtocol().name,
                onClick = {
                    viewModel.navigateToProtocolSettings()
                },
            )
            Divider(color = LocalColors.current.outline)
            SettingsItem(
                iconId = R.drawable.ic_setting_network,
                titleId = R.string.networks,
                onClick = {
                    viewModel.navigateToNetworkSettings()
                },
            )
            Divider(color = LocalColors.current.outline)
            SettingsItem(
                iconId = R.drawable.ic_setting_privacy,
                titleId = R.string.privacy,
                onClick = {
                    viewModel.navigateToPrivacySettings()
                },
            )
            Divider(color = LocalColors.current.outline)
            SettingsItem(
                iconId = R.drawable.ic_setting_automation,
                titleId = R.string.automation,
                subtitle = stringResource(id = if (viewModel.isAutomationEnabled()) R.string.enabled else R.string.disabled),
                onClick = {
                    viewModel.navigateToAutomation()
                },
            )
            Divider(color = LocalColors.current.outline)
            SettingsItem(
                iconId = R.drawable.ic_setting_help,
                titleId = R.string.help,
                onClick = {
                    viewModel.navigateToHelpSettings()
                },
            )
        }
    }
}

@Preview
@Composable
fun PreviewSettingsScreen() {
    SettingsScreen()
}