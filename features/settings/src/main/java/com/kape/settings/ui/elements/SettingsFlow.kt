package com.kape.settings.ui.elements

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kape.router.Settings
import com.kape.settings.ui.screens.GeneralSettingsScreen
import com.kape.settings.ui.screens.ProtocolSettingsScreen
import com.kape.settings.ui.screens.SettingsScreen

@Composable
fun SettingsFlow() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Settings.Main) {
        composable(Settings.Main) { SettingsScreen() }
        composable(Settings.General) { GeneralSettingsScreen() }
        composable(Settings.General) { ProtocolSettingsScreen() }
    }
}