package com.kape.vpn

import android.app.Activity.ScreenCaptureCallback
import android.app.ComponentCaller
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kape.about.screens.mobile.AboutScreen
import com.kape.about.screens.tv.TvAboutScreen
import com.kape.automation.ui.AutomationFlow
import com.kape.connection.ui.mobile.ConnectionScreen
import com.kape.connection.ui.tv.TvConnectionScreen
import com.kape.customization.CustomizationScreen
import com.kape.dedicatedip.domain.ObserveScreenCaptureUseCase
import com.kape.dedicatedip.ui.screens.mobile.DedicatedIpFlow
import com.kape.dedicatedip.ui.screens.tv.TvDedicatedIpScreen
import com.kape.dedicatedip.utils.DedicatedIpStep
import com.kape.inappbrowser.ui.InAppBrowser
import com.kape.login.ui.mobile.loginNavigation
import com.kape.login.ui.tv.LoginPasswordScreen
import com.kape.login.ui.tv.LoginUsernameScreen
import com.kape.login.utils.TokenAuthenticationUtil
import com.kape.obfuscationregionselection.ui.ShadowsocksRegionSelectionScreen
import com.kape.payments.ui.VpnSubscriptionPaymentProvider
import com.kape.permissions.utils.mobile.PermissionsFlow
import com.kape.permissions.utils.tv.TvPermissionsFlow
import com.kape.profile.ui.screens.mobile.AccountDeletedScreen
import com.kape.profile.ui.screens.mobile.ProfileScreen
import com.kape.profile.ui.screens.tv.TvProfileScreen
import com.kape.router.About
import com.kape.router.AccountDeleted
import com.kape.router.Automation
import com.kape.router.Connection
import com.kape.router.Customization
import com.kape.router.DedicatedIp
import com.kape.router.Default
import com.kape.router.EnterFlow
import com.kape.router.Login
import com.kape.router.NavigateBack
import com.kape.router.NavigateOut
import com.kape.router.PerAppSettings
import com.kape.router.Permissions
import com.kape.router.Profile
import com.kape.router.Router
import com.kape.router.Settings
import com.kape.router.ShadowsocksRegionSelection
import com.kape.router.Splash
import com.kape.router.Subscribe
import com.kape.router.TvHelp
import com.kape.router.TvLogin
import com.kape.router.TvSideMenu
import com.kape.router.TvWelcome
import com.kape.router.Update
import com.kape.router.VpnRegionSelection
import com.kape.router.WebContent
import com.kape.settings.ui.screens.mobile.AutomationSettingsScreen
import com.kape.settings.ui.screens.mobile.KillSwitchSettingScreen
import com.kape.settings.ui.screens.mobile.PerAppSettingsScreen
import com.kape.settings.ui.screens.mobile.ProtocolSettingsScreen
import com.kape.settings.ui.screens.mobile.SettingsFlow
import com.kape.settings.ui.screens.tv.TvPerAppSettingsScreen
import com.kape.settings.ui.screens.tv.TvSettingsFlow
import com.kape.settings.utils.SettingsStep
import com.kape.shortcut.prefs.ShortcutPrefs
import com.kape.sidemenu.ui.screens.tv.TvSideMenuScreen
import com.kape.signup.ui.mobile.SignupScreensFlow
import com.kape.signup.ui.tv.TvSignupScreensFlow
import com.kape.splash.ui.SplashScreen
import com.kape.splash.ui.UpdateScreen
import com.kape.tvwelcome.ui.TvWelcomeScreen
import com.kape.ui.theme.PIATheme
import com.kape.ui.theme.PiaScreen
import com.kape.utils.PlatformUtils
import com.kape.vpnregionselection.ui.mobile.VpnRegionSelectionScreen
import com.kape.vpnregionselection.ui.tv.TvVpnRegionSelectionScreen
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity() {
    private val router: Router by inject()
    private val tokenAuthenticationUtil: TokenAuthenticationUtil by inject()
    private val vpnSubscriptionPaymentProvider: VpnSubscriptionPaymentProvider by inject()
    private val observeScreenCaptureUseCase: ObserveScreenCaptureUseCase by inject()
    private val shortcutPrefs: ShortcutPrefs by inject()
    private lateinit var screenCaptureCallback: ScreenCaptureCallback
    private var currentDestination: String = ""
    private val destinationsForClearBackStack =
        listOf(
            Splash.Main,
            Subscribe.Main,
            Permissions.Route,
            Connection.Main,
            AccountDeleted.Route,
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vpnSubscriptionPaymentProvider.register(this)
        defineScreenOrientation()
        deepLinkLogin(intent)
        intent.action?.let {
            when (it) {
                Settings.Route -> {
                    shortcutPrefs.setShortcutSettings(true)
                }

                VpnRegionSelection.Main -> {
                    shortcutPrefs.setShortcutChangeServer(true)
                }

                Connection.Main -> {
                    shortcutPrefs.setShortcutConnectToVpn(true)
                }
            }
        }
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()

            LaunchedEffect(key1 = Unit) {
                repeatOnLifecycle(Lifecycle.State.CREATED) {
                    router.getNavigation().collect {
                        when (it) {
                            NavigateBack -> navController.navigateUp()
                            NavigateOut -> {
                                finishAndRemoveTask()
                                router.resetNavigation()
                            }

                            Default.Route -> {
                                // default state, don't do anything
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
                            defineNavigationGraph(navController = navController)
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (currentDestination == Subscribe.Main) {
            vpnSubscriptionPaymentProvider.getPurchaseUpdates()
        }
    }

    override fun onStart() {
        super.onStart()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            screenCaptureCallback = ScreenCaptureCallback {
                observeScreenCaptureUseCase.announce()
            }
            registerScreenCaptureCallback(mainExecutor, screenCaptureCallback)
        }
    }

    override fun onStop() {
        super.onStop()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            unregisterScreenCaptureCallback(screenCaptureCallback)
        }
    }

    override fun onNewIntent(intent: Intent, caller: ComponentCaller) {
        super.onNewIntent(intent, caller)
        deepLinkLogin(intent)
    }

    // region private
    private fun defineScreenOrientation() {
        if (PlatformUtils.isTv(context = this)) {
            this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    private fun deepLinkLogin(intent: Intent?) {
        intent?.data?.let {
            if (it.toString().contains("login")) {
                tokenAuthenticationUtil.authenticate(it)
            }
        }
    }

    private fun NavGraphBuilder.defineNavigationGraph(navController: NavController) {
        if (PlatformUtils.isTv(context = this@MainActivity)) {
            composable(Splash.Main) { SplashScreen() }
            composable(TvWelcome.Main) { TvWelcomeScreen() }
            composable(Subscribe.Main) { TvSignupScreensFlow() }
            composable(TvLogin.Username) { LoginUsernameScreen() }
            composable(Login.WithCredentials) { LoginPasswordScreen() }
            composable(Permissions.Route) { TvPermissionsFlow() }
            composable(Connection.Main) { TvConnectionScreen() }
            composable(VpnRegionSelection.Main) { TvVpnRegionSelectionScreen() }
            composable(WebContent.Terms) {
                InAppBrowser(url = getString(com.kape.ui.R.string.url_terms_of_service))
            }
            composable(WebContent.Privacy) {
                InAppBrowser(url = getString(com.kape.ui.R.string.url_privacy_policy))
            }
            composable(Settings.Route) { TvSettingsFlow(initialStep = SettingsStep.Main) }
            composable(TvSideMenu.Main) { TvSideMenuScreen() }
            composable(Profile.Main) { TvProfileScreen() }
            composable(DedicatedIp.ActivateToken) { TvDedicatedIpScreen() }
            composable(PerAppSettings.Main) { TvPerAppSettingsScreen() }
            composable(TvHelp.Main) { TvSettingsFlow(initialStep = SettingsStep.Help) }
            composable(About.Main) { TvAboutScreen() }
        } else {
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
            composable(WebContent.NoInAppRegistration) {
                InAppBrowser(url = getString(com.kape.ui.R.string.url_registration))
            }
            composable(WebContent.DeleteAccount) {
                InAppBrowser(url = getString(com.kape.ui.R.string.url_delete_account))
            }
            composable(PerAppSettings.Main) {
                PerAppSettingsScreen()
            }
            composable(DedicatedIp.ActivateToken) {
                DedicatedIpFlow(initialStep = DedicatedIpStep.ActivateToken)
            }
            composable(DedicatedIp.SignupPlans) {
                DedicatedIpFlow(initialStep = DedicatedIpStep.SignupPlans)
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
            composable(AccountDeleted.Route) {
                AccountDeletedScreen()
            }
            composable(Update.Route) {
                UpdateScreen()
            }
        }
    }
    // endregion
}