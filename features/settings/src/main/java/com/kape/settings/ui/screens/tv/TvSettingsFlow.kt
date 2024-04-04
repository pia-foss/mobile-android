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
        SettingsStep.Automation,
        SettingsStep.ShortcutAutomation,
        -> throw IllegalStateException("Unsupported on TV")
        SettingsStep.Obfuscation -> throw IllegalStateException("Unsupported on TV")
        SettingsStep.ConnectionStats -> throw IllegalStateException("Unsupported on TV")
        SettingsStep.DebugLogs -> throw IllegalStateException("Unsupported on TV")
        SettingsStep.General -> TvGeneralSettingsScreen()
        SettingsStep.Help -> TODO()
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