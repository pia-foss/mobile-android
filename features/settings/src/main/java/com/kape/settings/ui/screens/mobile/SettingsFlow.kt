package com.kape.settings.ui.screens.mobile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kape.router.LocalNavigator
import com.kape.settings.ui.vm.SettingsViewModel
import com.kape.settings.utils.SettingsStep
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsFlow() {
    val viewModel: SettingsViewModel = koinViewModel()
    

//    when (state) {
//        SettingsStep.Automation, SettingsStep.ShortcutAutomation -> AutomationSettingsScreen()
//        SettingsStep.Obfuscation -> ObfuscationSettingsScreen()
//        SettingsStep.ConnectionStats -> ConnectionStatsScreen()
//        SettingsStep.DebugLogs -> VpnLogScreen()
//        SettingsStep.General -> GeneralSettingsScreen()
//        SettingsStep.Help -> HelpScreen()
//        SettingsStep.KillSwitch, SettingsStep.ShortcutKillSwitch -> KillSwitchSettingScreen()
//        SettingsStep.Main -> SettingsScreen()
//        SettingsStep.Network -> NetworkSettingsScreen()
//        SettingsStep.Privacy -> PrivacySettingsScreen()
//        SettingsStep.Protocol, SettingsStep.ShortcutProtocol -> ProtocolSettingsScreen()
//        SettingsStep.ExternalProxyAppList -> ExternalProxyAppList()
//    }
}