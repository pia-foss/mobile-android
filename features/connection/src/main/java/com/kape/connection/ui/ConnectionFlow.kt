package com.kape.connection.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kape.router.Connection

@Composable
fun ConnectionFlow() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Connection.Main) {
        composable(Connection.Main) { ConnectionScreen() }
    }
}