package com.kape.settings.ui.screens.tv

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.kape.settings.ui.vm.SettingsViewModel
import com.kape.settings.utils.SettingsStep
import org.koin.androidx.compose.koinViewModel

@Composable
fun TvSettingsFlow() {
    val viewModel: SettingsViewModel = koinViewModel()
    val state by remember(viewModel) { viewModel.state }.collectAsState()

    when (state) {
        SettingsStep.Automation, SettingsStep.ShortcutAutomation -> TODO()
        SettingsStep.Obfuscation -> TODO()
        SettingsStep.ConnectionStats -> TODO()
        SettingsStep.DebugLogs -> TODO()
        SettingsStep.General -> TODO()
        SettingsStep.Help -> TODO()
        SettingsStep.KillSwitch, SettingsStep.ShortcutKillSwitch -> TODO()
        SettingsStep.Main -> TvSettingsScreen()
        SettingsStep.Network -> TODO()
        SettingsStep.Privacy -> TODO()
        SettingsStep.Protocol, SettingsStep.ShortcutProtocol -> TODO()
        SettingsStep.ExternalProxyAppList -> TODO()
    }
}