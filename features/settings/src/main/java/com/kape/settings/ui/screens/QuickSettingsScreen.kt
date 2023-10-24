package com.kape.settings.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kape.appbar.view.AppBar
import com.kape.appbar.viewmodel.AppBarViewModel
import com.kape.settings.R
import com.kape.settings.ui.elements.SettingsToggle
import com.kape.settings.ui.vm.SettingsViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun QuickSettingsScreen() {
    val viewModel: SettingsViewModel = koinViewModel()
    val appBarViewModel: AppBarViewModel = koinViewModel()
    Scaffold(
        topBar = {
            AppBar(
                viewModel = appBarViewModel,
            ) {
                viewModel.exitQuickSettings()
            }
        },
    ) {
        Column(
            modifier = Modifier
                .padding(it),
        ) {
            SettingsToggle(
                titleId = R.string.quick_setting_kill_switch,
                enabled = viewModel.isQuickSettingKillSwitchEnabled(),
                iconId = com.kape.ui.R.drawable.ic_killswitch,
                toggle = {
                    viewModel.toggleQuickSettingKillSwitch(it)
                },
            )
            SettingsToggle(
                titleId = R.string.quick_setting_automation,
                enabled = viewModel.isQuickSettingAutomationEnabled(),
                iconId = com.kape.ui.R.drawable.ic_network_management_inactive,
                toggle = {
                    viewModel.toggleQuickSettingAutomation(it)
                },
            )
            SettingsToggle(
                titleId = R.string.quick_setting_private_browser,
                enabled = viewModel.isQuickSettingPrivateBrowserEnabled(),
                iconId = com.kape.ui.R.drawable.ic_private_browser,
                toggle = {
                    viewModel.toggleQuickSettingPrivateBrowser(it)
                },
            )
        }
    }
}