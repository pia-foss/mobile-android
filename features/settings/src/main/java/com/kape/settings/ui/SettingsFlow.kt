package com.kape.settings.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kape.router.Settings

@Composable
fun SettingsFlow() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Settings.Main) {
        composable(Settings.Main) { SettingsScreen() }
        composable(Settings.General) { GeneralSettingsScreen() }
    }
}