package com.kape.settings.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.kape.settings.ui.screens.AutomationSettingsScreen
import com.kape.settings.ui.screens.ConnectionStatsScreen
import com.kape.settings.ui.screens.GeneralSettingsScreen
import com.kape.settings.ui.screens.HelpScreen
import com.kape.settings.ui.screens.KillSwitchSettingScreen
import com.kape.settings.ui.screens.NetworkSettingsScreen
import com.kape.settings.ui.screens.PrivacySettingsScreen
import com.kape.settings.ui.screens.ProtocolSettingsScreen
import com.kape.settings.ui.screens.SettingsScreen
import com.kape.settings.ui.screens.VpnLogScreen
import com.kape.settings.ui.vm.SettingsViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsFlow() {
    val viewModel: SettingsViewModel = koinViewModel()
    val state by remember(viewModel) { viewModel.state }.collectAsState()

    when (state) {
        SettingsStep.Automation, SettingsStep.ShortcutAutomation -> AutomationSettingsScreen()
        SettingsStep.ConnectionStats -> ConnectionStatsScreen()
        SettingsStep.DebugLogs -> VpnLogScreen()
        SettingsStep.General -> GeneralSettingsScreen()
        SettingsStep.Help -> HelpScreen()
        SettingsStep.KillSwitch, SettingsStep.ShortcutKillSwitch -> KillSwitchSettingScreen()
        SettingsStep.Main -> SettingsScreen()
        SettingsStep.Network -> NetworkSettingsScreen()
        SettingsStep.Privacy -> PrivacySettingsScreen()
        SettingsStep.Protocol, SettingsStep.ShortcutProtocol -> ProtocolSettingsScreen()
    }
}