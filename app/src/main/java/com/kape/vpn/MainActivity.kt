package com.kape.vpn

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
import com.kape.about.AboutScreen
import com.kape.automation.ui.AutomationFlow
import com.kape.connection.ui.ConnectionScreen
import com.kape.customization.CustomizationScreen
import com.kape.dedicatedip.ui.DedicatedIpScreen
import com.kape.inappbrowser.ui.InAppBrowser
import com.kape.login.ui.loginNavigation
import com.kape.obfuscationregionselection.ui.ShadowsocksRegionSelectionScreen
import com.kape.payments.ui.PaymentProvider
import com.kape.permissions.utils.PermissionsFlow
import com.kape.profile.ui.ProfileScreen
import com.kape.router.About
import com.kape.vpnregionselection.ui.VpnRegionSelectionScreen
import com.kape.router.Automation
import com.kape.router.Connection
import com.kape.router.Customization
import com.kape.router.DedicatedIp
import com.kape.router.EnterFlow
import com.kape.router.NavigateBack
import com.kape.router.NavigateOut
import com.kape.router.PerAppSettings
import com.kape.router.Permissions
import com.kape.router.Profile
import com.kape.router.VpnRegionSelection
import com.kape.router.Router
import com.kape.router.Settings
import com.kape.router.ShadowsocksRegionSelection
import com.kape.router.Splash
import com.kape.router.Subscribe
import com.kape.router.WebContent
import com.kape.settings.ui.screens.AutomationSettingsScreen
import com.kape.settings.ui.screens.KillSwitchSettingScreen
import com.kape.settings.ui.screens.PerAppSettingsScreen
import com.kape.settings.ui.screens.ProtocolSettingsScreen
import com.kape.settings.utils.SettingsFlow
import com.kape.signup.ui.SignupScreensFlow
import com.kape.splash.ui.SplashScreen
import com.kape.ui.theme.PIATheme
import com.kape.ui.theme.PiaScreen
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val router: Router by inject()

    private val paymentProvider: PaymentProvider by inject()
    private var currentDestination: String = ""
    private val destinationsForClearBackStack =
        listOf(
            Splash.Main,
            Subscribe.Main,
            Permissions.Route,
            Connection.Main,
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        paymentProvider.register(this)

        intent.action?.let {
            when (it) {
                Settings.Route -> router.handleFlow(EnterFlow.Settings)
                VpnRegionSelection.Main -> router.handleFlow(EnterFlow.VpnRegionSelection)
                Connection.Main -> router.handleFlow(EnterFlow.Connection)
            }
        }

        setContent {
            val navController = rememberNavController()

            LaunchedEffect(key1 = Unit) {
                repeatOnLifecycle(Lifecycle.State.CREATED) {
                    router.navigation.collect {
                        when (it) {
                            NavigateBack -> navController.navigateUp()
                            NavigateOut -> {
                                finishAndRemoveTask()
                                router.resetNavigation()
                            }

                            else -> {
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
                        color = MaterialTheme.colorScheme.background,
                    ) {
                        NavHost(navController = navController, startDestination = Splash.Main) {
                            loginNavigation(navController)
                            composable(Settings.Route) { SettingsFlow() }
                            composable(Permissions.Route) { PermissionsFlow() }
                            composable(Automation.Route) { AutomationFlow() }
                            composable(Splash.Main) { SplashScreen() }
                            composable(Connection.Main) { ConnectionScreen() }
                            composable(Profile.Main) { ProfileScreen() }
                            composable(Subscribe.Main) { SignupScreensFlow() }
                            composable(VpnRegionSelection.Main) { VpnRegionSelectionScreen() }
                            composable(ShadowsocksRegionSelection.Main) {
                                ShadowsocksRegionSelectionScreen()
                            }
                            composable(WebContent.Terms) {
                                InAppBrowser(
                                    url = getString(com.kape.ui.R.string.url_terms_of_service),
                                )
                            }
                            composable(WebContent.Privacy) {
                                InAppBrowser(
                                    url = getString(com.kape.ui.R.string.url_privacy_policy),
                                )
                            }
                            composable(WebContent.Support) {
                                InAppBrowser(
                                    url = getString(com.kape.ui.R.string.url_support),
                                )
                            }
                            composable(PerAppSettings.Main) {
                                PerAppSettingsScreen()
                            }
                            composable(DedicatedIp.Main) {
                                DedicatedIpScreen()
                            }
                            composable(Settings.Automation) {
                                AutomationSettingsScreen()
                            }
                            composable(Settings.KillSwitch) {
                                KillSwitchSettingScreen()
                            }
                            composable(Settings.Protocols) {
                                ProtocolSettingsScreen()
                            }
                            composable(About.Main) {
                                AboutScreen()
                            }
                            composable(Customization.Route) {
                                CustomizationScreen()
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