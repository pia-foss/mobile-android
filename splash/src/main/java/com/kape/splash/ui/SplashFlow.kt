package com.kape.splash.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.kape.router.Splash

@Composable
fun SplashFlow() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Splash.Main) {
        composable(Splash.Main) { SplashScreen() }
    }
}