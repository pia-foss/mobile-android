package com.kape.vpn

import android.app.ComponentCaller
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.kape.about.screens.mobile.AboutScreen
import com.kape.about.screens.tv.TvAboutScreen
import com.kape.automation.ui.screens.AddNewRuleScreen
import com.kape.automation.ui.screens.AutomationScreen
import com.kape.automation.ui.screens.BackgroundLocationPermissionScreen
import com.kape.automation.ui.screens.LocationPermissionScreen
import com.kape.connection.ui.mobile.ConnectionScreen
import com.kape.connection.ui.tv.TvConnectionScreen
import com.kape.customization.CustomizationScreen
import com.kape.dedicatedip.ui.screens.mobile.DedicatedIpScreen
import com.kape.dedicatedip.ui.screens.mobile.SignupDedicatedIpCountryScreen
import com.kape.dedicatedip.ui.screens.mobile.SignupDedicatedIpPurchaseSuccessScreen
import com.kape.dedicatedip.ui.screens.mobile.SignupDedicatedIpScreen
import com.kape.dedicatedip.ui.screens.mobile.SignupDedicatedIpTokenActivateScreen
import com.kape.dedicatedip.ui.screens.mobile.SignupDedicatedIpTokenDetailsScreen
import com.kape.dedicatedip.ui.screens.tv.TvDedicatedIpScreen
import com.kape.dedicatedip.utils.DedicatedIpStep
import com.kape.inappbrowser.ui.InAppBrowser
import com.kape.login.ui.mobile.LoginScreen
import com.kape.login.ui.mobile.LoginWithEmailScreen
import com.kape.login.ui.tv.LoginPasswordScreen
import com.kape.login.ui.tv.LoginUsernameScreen
import com.kape.login.utils.TokenAuthenticationUtil
import com.kape.obfuscationregionselection.ui.ShadowsocksRegionSelectionScreen
import com.kape.payments.ui.VpnSubscriptionPaymentProvider
import com.kape.permissions.ui.mobile.NotificationPermissionScreen
import com.kape.permissions.ui.mobile.VpnPermissionScreen
import com.kape.permissions.ui.tv.TvNotificationPermissionScreen
import com.kape.permissions.ui.tv.TvVpnPermissionScreen
import com.kape.permissions.utils.PermissionUtil
import com.kape.profile.ui.screens.mobile.AccountDeletedScreen
import com.kape.profile.ui.screens.mobile.ProfileScreen
import com.kape.profile.ui.screens.tv.TvProfileScreen
import com.kape.router.About
import com.kape.router.AccountDeleted
import com.kape.router.AutomationAddRule
import com.kape.router.AutomationBackgroundLocation
import com.kape.router.AutomationLocation
import com.kape.router.AutomationMain
import com.kape.router.AutomationSettings
import com.kape.router.Connection
import com.kape.router.Customization
import com.kape.router.DedicatedIpActivateToken
import com.kape.router.DedicatedIpLocationSelection
import com.kape.router.DedicatedIpPurchaseSuccess
import com.kape.router.DedicatedIpSignupPlans
import com.kape.router.DedicatedIpSignupTokenActivate
import com.kape.router.DedicatedIpSignupTokenDetails
import com.kape.router.GeneralSettings
import com.kape.router.HelpSettings
import com.kape.router.KillSwitchSettings
import com.kape.router.LoginWithCredentials
import com.kape.router.LoginWithEmail
import com.kape.router.NetworkSettings
import com.kape.router.NotificationPermission
import com.kape.router.PerAppSettings
import com.kape.router.PrivacySettings
import com.kape.router.Profile
import com.kape.router.ProtocolSettings
import com.kape.router.Router
import com.kape.router.Settings
import com.kape.router.ShadowsocksRegionSelection
import com.kape.router.Splash
import com.kape.router.Subscribe
import com.kape.router.TvHelp
import com.kape.router.TvLoginPassword
import com.kape.router.TvLoginUsername
import com.kape.router.TvSideMenu
import com.kape.router.TvWelcome
import com.kape.router.Update
import com.kape.router.VpnPermission
import com.kape.router.VpnRegionSelection
import com.kape.router.WebDestination
import com.kape.settings.ui.screens.mobile.AutomationSettingsScreen
import com.kape.settings.ui.screens.mobile.GeneralSettingsScreen
import com.kape.settings.ui.screens.mobile.HelpScreen
import com.kape.settings.ui.screens.mobile.KillSwitchSettingScreen
import com.kape.settings.ui.screens.mobile.NetworkSettingsScreen
import com.kape.settings.ui.screens.mobile.PerAppSettingsScreen
import com.kape.settings.ui.screens.mobile.PrivacySettingsScreen
import com.kape.settings.ui.screens.mobile.ProtocolSettingsScreen
import com.kape.settings.ui.screens.mobile.SettingsScreen
import com.kape.settings.ui.screens.tv.TvHelpScreen
import com.kape.settings.ui.screens.tv.TvPerAppSettingsScreen
import com.kape.settings.ui.screens.tv.TvSettingsScreen
import com.kape.shortcut.prefs.ShortcutPrefs
import com.kape.sidemenu.ui.screens.tv.TvSideMenuScreen
import com.kape.signup.ui.mobile.SignupScreensFlow
import com.kape.signup.ui.tv.TvSignupScreensFlow
import com.kape.splash.ui.SplashScreen
import com.kape.splash.ui.UpdateScreen
import com.kape.tvwelcome.ui.TvWelcomeScreen
import com.kape.ui.theme.PIATheme
import com.kape.ui.theme.PiaScreen
import com.kape.ui.utils.ExternallyUsed.Constants.ACTION_CONNECT
import com.kape.ui.utils.ExternallyUsed.Constants.ACTION_SERVER_SELECTION
import com.kape.ui.utils.ExternallyUsed.Constants.ACTION_SETTINGS
import com.kape.utils.PlatformUtils
import com.kape.vpnregionselection.ui.mobile.VpnRegionSelectionScreen
import com.kape.vpnregionselection.ui.tv.TvVpnRegionSelectionScreen
import org.koin.android.ext.android.inject
import org.koin.core.qualifier.named

class MainActivity : AppCompatActivity() {
    private val router: Router by inject()
    private val tokenAuthenticationUtil: TokenAuthenticationUtil by inject()
    private val vpnSubscriptionPaymentProvider: VpnSubscriptionPaymentProvider by inject()
    private val shortcutPrefs: ShortcutPrefs by inject()
    val licences: List<String> by inject(named("licences"))
    val permissionUtil: PermissionUtil by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE,
        )
        vpnSubscriptionPaymentProvider.register(this)
        defineScreenOrientation()
        deepLinkLogin(intent)
        intent.action?.let {
            when (it) {
                ACTION_SETTINGS -> {
                    shortcutPrefs.setShortcutSettings(true)
                }

                ACTION_SERVER_SELECTION -> {
                    shortcutPrefs.setShortcutChangeServer(true)
                }

                ACTION_CONNECT -> {
                    shortcutPrefs.setShortcutConnectToVpn(true)
                }
            }
        }
        enableEdgeToEdge()
        setContent {
            PIATheme {
                PiaScreen(router = router) { navController ->
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background,
                    ) {
                        NavHost(navController = navController, startDestination = Splash) {
                            defineNavigationGraph(licences)
                        }
                    }
                }
            }
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

    private fun NavGraphBuilder.defineNavigationGraph(licences: List<String>) {
        if (PlatformUtils.isTv(context = this@MainActivity)) {
            composable<Splash> { SplashScreen() }
            composable<TvWelcome> { TvWelcomeScreen() }
            composable<Subscribe> { TvSignupScreensFlow() }
            composable<TvLoginUsername> { LoginUsernameScreen() }
            composable<TvLoginPassword> { LoginPasswordScreen() }
            composable<VpnPermission> { TvVpnPermissionScreen() }
            composable<NotificationPermission> { TvNotificationPermissionScreen() }
            composable<Connection> { TvConnectionScreen() }
            composable<VpnRegionSelection> { TvVpnRegionSelectionScreen() }
            composable<WebDestination.Terms> {
                InAppBrowser(url = getString(com.kape.ui.R.string.url_terms_of_service))
            }
            composable<WebDestination.Privacy> {
                InAppBrowser(url = getString(com.kape.ui.R.string.url_privacy_policy))
            }
            composable<Settings> { TvSettingsScreen() }
            composable<TvSideMenu> { TvSideMenuScreen() }
            composable<Profile> { TvProfileScreen() }
            composable<DedicatedIpActivateToken> { TvDedicatedIpScreen() }
            composable<PerAppSettings> { TvPerAppSettingsScreen() }
            composable<TvHelp> { TvHelpScreen() }
            composable<About> { TvAboutScreen(licences) }
        } else {
            composable<LoginWithCredentials> { LoginScreen() }
            composable<LoginWithEmail> { LoginWithEmailScreen() }
            composable<Settings> { SettingsScreen() }
            composable<VpnPermission> { VpnPermissionScreen() }
            composable<NotificationPermission> { NotificationPermissionScreen() }
            composable<AutomationSettings> { AutomationSettingsScreen() }
            composable<AutomationMain> { AutomationScreen() }
            composable<AutomationAddRule> { AddNewRuleScreen() }
            composable<AutomationLocation> { LocationPermissionScreen() }
            composable<AutomationBackgroundLocation> { BackgroundLocationPermissionScreen() }
            composable<Splash> { SplashScreen() }
            composable<Connection> { ConnectionScreen(exitApp = { finishAndRemoveTask() }) }
            composable<Profile> { ProfileScreen() }
            composable<Subscribe> { SignupScreensFlow() }
            composable<VpnRegionSelection> { VpnRegionSelectionScreen() }
            composable<ShadowsocksRegionSelection> {
                ShadowsocksRegionSelectionScreen()
            }
            composable<WebDestination.Terms> {
                InAppBrowser(
                    url = getString(com.kape.ui.R.string.url_terms_of_service),
                )
            }
            composable<WebDestination.Privacy> {
                InAppBrowser(
                    url = getString(com.kape.ui.R.string.url_privacy_policy),
                )
            }
            composable<WebDestination.Support> {
                InAppBrowser(
                    url = getString(com.kape.ui.R.string.url_support),
                )
            }
            composable<WebDestination.NoInAppRegistration> {
                InAppBrowser(url = getString(com.kape.ui.R.string.url_registration))
            }
            composable<WebDestination.DeleteAccount> {
                InAppBrowser(url = getString(com.kape.ui.R.string.url_delete_account))
            }
            composable<PerAppSettings> { PerAppSettingsScreen() }
            composable<DedicatedIpActivateToken> { DedicatedIpScreen() }
            composable<DedicatedIpSignupPlans> { SignupDedicatedIpScreen() }
            composable<DedicatedIpSignupTokenDetails> { SignupDedicatedIpTokenDetailsScreen() }
            composable<DedicatedIpLocationSelection> { SignupDedicatedIpCountryScreen() }
            composable<DedicatedIpSignupTokenActivate> { SignupDedicatedIpTokenActivateScreen() }
            composable<DedicatedIpPurchaseSuccess> { SignupDedicatedIpPurchaseSuccessScreen() }
            composable<KillSwitchSettings> { KillSwitchSettingScreen() }
            composable<ProtocolSettings> { ProtocolSettingsScreen() }
            composable<About> { AboutScreen(licences) }
            composable<Customization> { CustomizationScreen() }
            composable<AccountDeleted> {
                AccountDeletedScreen({ finishAndRemoveTask() })
            }
            composable<Update> { UpdateScreen() }
            composable<GeneralSettings> { GeneralSettingsScreen() }
            composable<ProtocolSettings> { ProtocolSettingsScreen() }
            composable<NetworkSettings> { NetworkSettingsScreen() }
            composable<PrivacySettings> { PrivacySettingsScreen() }
            composable<HelpSettings> { HelpScreen() }
        }
    }
    // endregion
}