package com.kape.settings.ui

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.kape.router.Settings
import com.kape.settings.ui.screens.AutomationSettingsScreen
import com.kape.settings.ui.screens.ConnectionStatsScreen
import com.kape.settings.ui.screens.GeneralSettingsScreen
import com.kape.settings.ui.screens.HelpScreen
import com.kape.settings.ui.screens.KillSwitchSettingScreen
import com.kape.settings.ui.screens.NetworkSettingsScreen
import com.kape.settings.ui.screens.PrivacySettingsScreen
import com.kape.settings.ui.screens.ProtocolSettingsScreen
import com.kape.settings.ui.screens.QuickSettingsScreen
import com.kape.settings.ui.screens.SettingsScreen
import com.kape.settings.ui.screens.VpnLogScreen
import com.kape.settings.ui.screens.WidgetSettingsScreen

fun NavGraphBuilder.settingsNavigation(navController: NavController) {
    navigation(startDestination = Settings.Main, route = Settings.Route) {
        composable(Settings.Main) {
            SettingsScreen()
        }
        composable(Settings.General) {
            GeneralSettingsScreen()
        }
        composable(Settings.Protocols) {
            ProtocolSettingsScreen()
        }
        composable(Settings.Networks) {
            NetworkSettingsScreen()
        }
        composable(Settings.Privacy) {
            PrivacySettingsScreen()
        }
        composable(Settings.Automation) {
            AutomationSettingsScreen()
        }
        composable(Settings.Help) {
            HelpScreen()
        }
        composable(Settings.KillSwitch) {
            KillSwitchSettingScreen()
        }
        composable(Settings.QuickSettings) {
            QuickSettingsScreen()
        }
        composable(Settings.ConnectionStats) {
            ConnectionStatsScreen()
        }
        composable(Settings.DebugLogs) {
            VpnLogScreen()
        }
        composable(Settings.Widget) {
            WidgetSettingsScreen()
        }
    }
}