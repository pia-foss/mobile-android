package com.kape.settings.ui

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
                // TODO: remove hardcoded protocol
                subtitleId = R.string.open_vpn,
                onClick = {},
            )
            Divider(color = LocalColors.current.outline)
            SettingsItem(
                iconId = R.drawable.ic_setting_network,
                titleId = R.string.networks,
                onClick = {},
            )
            Divider(color = LocalColors.current.outline)
            SettingsItem(
                iconId = R.drawable.ic_setting_privacy,
                titleId = R.string.privacy,
                onClick = {},
            )
            Divider(color = LocalColors.current.outline)
            SettingsItem(
                iconId = R.drawable.ic_setting_automation,
                titleId = R.string.automation,
                // TODO: set subtitle id
//                subtitleId = R.string.settings_pia_settings_item_automation_disabled,
                onClick = {},
            )
            Divider(color = LocalColors.current.outline)
            SettingsItem(
                iconId = R.drawable.ic_setting_obfuscation,
                titleId = R.string.obfuscation,
                onClick = {},
            )
            Divider(color = LocalColors.current.outline)
            SettingsItem(
                iconId = R.drawable.ic_setting_help,
                titleId = R.string.help,
                onClick = {},
            )
        }
    }
}

@Preview
@Composable
fun PreviewSettingsScreen() {
    SettingsScreen()
}