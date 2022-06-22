package com.kape.vpn_permissions.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kape.router.VpnPermission

@Composable
fun VpnPermissionFlow() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = VpnPermission.Main) {
        composable(VpnPermission.Main) { VpnSystemProfileScreen() }
    }
}