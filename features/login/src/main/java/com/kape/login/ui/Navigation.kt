package com.kape.login.ui

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.kape.router.Login

fun NavGraphBuilder.loginNavigation(navController: NavController) {
    navigation(startDestination = Login.WithCredentials, route = Login.Route) {
        composable(Login.WithCredentials) { LoginScreen(navController = navController) }
        composable(Login.WithEmail) { LoginWithEmailScreen(navController = navController) }
    }
}