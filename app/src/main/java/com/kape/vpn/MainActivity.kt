package com.kape.vpn

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kape.connection.ui.ConnectionScreen
import com.kape.login.ui.LoginScreen
import com.kape.login.ui.LoginWithEmailScreen
import com.kape.payments.ui.PaymentProvider
import com.kape.profile.ui.ProfileScreen
import com.kape.regionselection.ui.RegionSelectionScreen
import com.kape.router.Connection
import com.kape.router.Login
import com.kape.router.NavigateBack
import com.kape.router.Profile
import com.kape.router.RegionSelection
import com.kape.router.Router
import com.kape.router.Settings
import com.kape.router.Splash
import com.kape.router.Subscribe
import com.kape.router.VpnPermission
import com.kape.router.WebContent
import com.kape.settings.ui.screens.GeneralSettingsScreen
import com.kape.settings.ui.screens.ProtocolSettingsScreen
import com.kape.settings.ui.screens.SettingsScreen
import com.kape.signup.ui.SignupScreensFlow
import com.kape.splash.ui.SplashScreen
import com.kape.ui.elements.WebViewScreen
import com.kape.ui.theme.PIATheme
import com.kape.ui.theme.PiaScreen
import com.kape.vpnpermission.ui.VpnSystemProfileScreen
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val router: Router by inject()

    private val paymentProvider: PaymentProvider by inject()
    private var currentDestination: String = ""
    private val destinationsForClearBackStack =
        listOf(Splash.Main, Subscribe.Main, VpnPermission.Main, Connection.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        paymentProvider.register(this)

        setContent {
            val navController = rememberNavController()

            LaunchedEffect(key1 = Unit) {
                repeatOnLifecycle(Lifecycle.State.CREATED) {
                    router.navigation.collect {
                        if (it.isNotBlank()) {
                            if (it == NavigateBack) {
                                navController.navigateUp()
                            } else {
                                currentDestination = it
                                navController.navigate(it) {
                                    launchSingleTop = true
                                    if (it in destinationsForClearBackStack) {
                                        navController.popBackStack()
                                    }
                                }
                            }
                        }
                    }
                }
            }

            PIATheme {
                PiaScreen {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        NavHost(navController = navController, startDestination = Splash.Main) {
                            composable(Login.Main) { LoginScreen(navController = navController) }
                            composable(Login.WithEmail) { LoginWithEmailScreen(navController = navController) }
                            composable(VpnPermission.Main) { VpnSystemProfileScreen() }
                            composable(Splash.Main) { SplashScreen() }
                            composable(Connection.Main) { ConnectionScreen() }
                            composable(RegionSelection.Main) { RegionSelectionScreen() }
                            composable(Profile.Main) { ProfileScreen() }
                            composable(Subscribe.Main) { SignupScreensFlow() }
                            composable(WebContent.Terms) {
                                WebViewScreen(
                                    initialUrl = Uri.parse(
                                        getString(R.string.url_terms_of_service)
                                    )
                                )
                            }
                            composable(WebContent.Privacy) {
                                WebViewScreen(
                                    initialUrl = Uri.parse(
                                        getString(R.string.url_privacy_policy)
                                    )
                                )
                            }
                            composable(WebContent.Survey) {
                                WebViewScreen(
                                    initialUrl = Uri.parse(
                                        getString(R.string.url_survey)
                                    )
                                )
                            }
                            composable(Settings.Main) {
                                SettingsScreen()
                            }
                            composable(Settings.General) {
                                GeneralSettingsScreen()
                            }
                            composable(Settings.Protocols) {
                                ProtocolSettingsScreen()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (currentDestination == Subscribe.Main) {
            paymentProvider.getPurchaseUpdates()
        }
    }
}