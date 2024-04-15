package com.kape.settings.ui.screens.tv

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.kape.settings.ui.vm.SettingsViewModel
import com.kape.settings.utils.SettingsStep
import org.koin.androidx.compose.koinViewModel

@Composable
fun TvSettingsFlow(initialStep: SettingsStep) {
    val viewModel: SettingsViewModel = koinViewModel()
    val state by remember(viewModel) { viewModel.state }.collectAsState()
    if (state == SettingsStep.Main) {
        when (initialStep) {
            SettingsStep.Automation -> viewModel.navigateToAutomation()
            SettingsStep.ConnectionStats -> viewModel.navigateToConnectionStats()
            SettingsStep.DebugLogs -> viewModel.navigateToDebugLogs()
            SettingsStep.ExternalProxyAppList -> viewModel.navigateToExternalAppList()
            SettingsStep.General -> viewModel.navigateToGeneralSettings()
            SettingsStep.Help -> viewModel.navigateToHelpSettings()
            SettingsStep.KillSwitch -> viewModel.navigateToKillSwitch()
            SettingsStep.Main -> { }
            SettingsStep.Network -> viewModel.navigateToNetworkSettings()
            SettingsStep.Obfuscation -> viewModel.navigateToObfuscationSettings()
            SettingsStep.Privacy -> viewModel.navigateToPrivacySettings()
            SettingsStep.Protocol -> viewModel.navigateToProtocolSettings()
            SettingsStep.ShortcutAutomation -> viewModel.navigateToAutomation()
            SettingsStep.ShortcutKillSwitch -> viewModel.navigateToKillSwitch()
            SettingsStep.ShortcutProtocol -> viewModel.navigateToProtocolSettings()
        }
    }

    when (state) {
        SettingsStep.Automation,
        SettingsStep.ShortcutAutomation,
        -> throw IllegalStateException("Unsupported on TV")
        SettingsStep.Obfuscation -> throw IllegalStateException("Unsupported on TV")
        SettingsStep.ConnectionStats -> TvConnectionStatsScreen()
        SettingsStep.DebugLogs -> TODO()
        SettingsStep.General -> TvGeneralSettingsScreen()
        SettingsStep.Help -> TvHelpScreen()
        SettingsStep.KillSwitch,
        SettingsStep.ShortcutKillSwitch,
        -> throw IllegalStateException("Unsupported on TV")
        SettingsStep.Main -> TvSettingsScreen()
        SettingsStep.Network -> TvNetworkSettingsScreen()
        SettingsStep.Privacy -> TvPrivacySettingsScreen()
        SettingsStep.Protocol,
        SettingsStep.ShortcutProtocol,
        -> TvProtocolSettingsScreen()
        SettingsStep.ExternalProxyAppList -> throw IllegalStateException("Unsupported on TV")
    }
}