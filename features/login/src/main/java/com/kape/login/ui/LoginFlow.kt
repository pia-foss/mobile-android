package com.kape.login.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kape.router.Login

@Composable
fun LoginFlow() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Login.Main) {
        composable(Login.Main) { LoginScreen(navController) }
        composable(Login.WithEmail) { LoginWithEmailScreen(navController) }
    }
}