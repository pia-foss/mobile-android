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
import com.kape.contracts.Router
import com.kape.customization.CustomizationScreen
import com.kape.dedicatedip.ui.screens.mobile.DedicatedIpScreen
import com.kape.dedicatedip.ui.screens.mobile.SignupDedicatedIpCountryScreen
import com.kape.dedicatedip.ui.screens.mobile.SignupDedicatedIpPurchaseSuccessScreen
import com.kape.dedicatedip.ui.screens.mobile.SignupDedicatedIpScreen
import com.kape.dedicatedip.ui.screens.mobile.SignupDedicatedIpTokenActivateScreen
import com.kape.dedicatedip.ui.screens.mobile.SignupDedicatedIpTokenDetailsScreen
import com.kape.dedicatedip.ui.screens.tv.TvDedicatedIpScreen
import com.kape.inappbrowser.ui.InAppBrowser
import com.kape.localprefs.prefs.ShortcutPrefs
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
import com.kape.data.About
import com.kape.data.AccountDeleted
import com.kape.data.AutomationAddRule
import com.kape.data.AutomationBackgroundLocation
import com.kape.data.AutomationLocation
import com.kape.data.AutomationMain
import com.kape.data.AutomationSettings
import com.kape.data.Connection
import com.kape.data.ConnectionStats
import com.kape.data.Customization
import com.kape.data.DebugLogs
import com.kape.data.DedicatedIpActivateToken
import com.kape.data.DedicatedIpLocationSelection
import com.kape.data.DedicatedIpPurchaseSuccess
import com.kape.data.DedicatedIpSignupPlans
import com.kape.data.DedicatedIpSignupTokenActivate
import com.kape.data.DedicatedIpSignupTokenDetails
import com.kape.data.ExternalAppList
import com.kape.data.GeneralSettings
import com.kape.data.HelpSettings
import com.kape.data.KillSwitchSettings
import com.kape.data.LoginWithCredentials
import com.kape.data.LoginWithEmail
import com.kape.data.NetworkSettings
import com.kape.data.NotificationPermission
import com.kape.data.ObfuscationSettings
import com.kape.data.PerAppSettings
import com.kape.data.PrivacySettings
import com.kape.data.Profile
import com.kape.data.ProtocolSettings
import com.kape.data.Settings
import com.kape.data.ShadowsocksRegionSelection
import com.kape.data.Subscribe
import com.kape.data.TvLoginPassword
import com.kape.data.TvLoginUsername
import com.kape.data.TvSideMenu
import com.kape.data.TvSubscribe
import com.kape.data.TvWelcome
import com.kape.data.Update
import com.kape.data.VpnPermission
import com.kape.data.VpnRegionSelection
import com.kape.data.WebDestination
import com.kape.data.Splash
import com.kape.settings.ui.screens.mobile.AutomationSettingsScreen
import com.kape.settings.ui.screens.mobile.ConnectionStatsScreen
import com.kape.settings.ui.screens.mobile.ExternalProxyAppList
import com.kape.settings.ui.screens.mobile.GeneralSettingsScreen
import com.kape.settings.ui.screens.mobile.HelpScreen
import com.kape.settings.ui.screens.mobile.KillSwitchSettingScreen
import com.kape.settings.ui.screens.mobile.NetworkSettingsScreen
import com.kape.settings.ui.screens.mobile.ObfuscationSettingsScreen
import com.kape.settings.ui.screens.mobile.PerAppSettingsScreen
import com.kape.settings.ui.screens.mobile.PrivacySettingsScreen
import com.kape.settings.ui.screens.mobile.ProtocolSettingsScreen
import com.kape.settings.ui.screens.mobile.SettingsScreen
import com.kape.settings.ui.screens.mobile.VpnLogScreen
import com.kape.settings.ui.screens.tv.TvConnectionStatsScreen
import com.kape.settings.ui.screens.tv.TvGeneralSettingsScreen
import com.kape.settings.ui.screens.tv.TvHelpScreen
import com.kape.settings.ui.screens.tv.TvNetworkSettingsScreen
import com.kape.settings.ui.screens.tv.TvPerAppSettingsScreen
import com.kape.settings.ui.screens.tv.TvPrivacySettingsScreen
import com.kape.settings.ui.screens.tv.TvProtocolSettingsScreen
import com.kape.settings.ui.screens.tv.TvSettingsScreen
import com.kape.sidemenu.ui.screens.tv.TvSideMenuScreen
import com.kape.signup.ui.mobile.SignupScreensFlow
import com.kape.signup.ui.tv.TvSignupScreensFlow
import com.kape.splash.ui.SplashScreen
import com.kape.splash.ui.UpdateScreen
import com.kape.tvwelcome.ui.TvWelcomeScreen
import com.kape.ui.theme.PIATheme
import com.kape.ui.theme.PiaScreen
import com.kape.ui.utils.ExternallyUsed.Constants.ACTION_CONNECT
import com.kape.ui.utils.ExternallyUsed.Constants.ACTION_DISCONNECT
import com.kape.ui.utils.ExternallyUsed.Constants.ACTION_SERVER_SELECTION
import com.kape.ui.utils.ExternallyUsed.Constants.ACTION_SETTINGS
import com.kape.utils.PlatformUtils
import com.kape.vpn.utils.ShortcutManager
import com.kape.vpnregionselection.ui.mobile.VpnRegionSelectionScreen
import com.kape.vpnregionselection.ui.tv.TvVpnRegionSelectionScreen
import org.koin.android.ext.android.inject
import org.koin.core.qualifier.named

class MainActivity : AppCompatActivity() {
    private val router: Router by inject()
    private val tokenAuthenticationUtil: TokenAuthenticationUtil by inject()
    private val vpnSubscriptionPaymentProvider: VpnSubscriptionPaymentProvider by inject()
    private val shortcutPrefs: ShortcutPrefs by inject()
    private val platformUtils: PlatformUtils by inject()
    private val shortcutManager: ShortcutManager by inject()
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
                ACTION_SETTINGS -> shortcutPrefs.setShortcutSettings(true)
                ACTION_SERVER_SELECTION -> shortcutPrefs.setShortcutChangeServer(true)
                ACTION_CONNECT -> shortcutPrefs.setShortcutConnectToVpn(true)
                ACTION_DISCONNECT -> shortcutPrefs.setShortcutDisconnectVpn(true)
            }
        }
        enableEdgeToEdge()
        setContent {
            PIATheme(platformUtils.isTv()) {
                PiaScreen(router = router, isTv = platformUtils.isTv()) { navController ->
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
        shortcutManager.createDynamicShortcuts()
    }

    override fun onNewIntent(intent: Intent, caller: ComponentCaller) {
        super.onNewIntent(intent, caller)
        deepLinkLogin(intent)
    }

    // region private
    private fun defineScreenOrientation() {
        if (platformUtils.isTv()) {
            this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE // tv
        } else if (platformUtils.isTablet()) {
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED  // allow rotation
        } else {
            this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT // phone
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
        if (platformUtils.isTv()) {
            composable<Splash> { SplashScreen() }
            composable<TvWelcome> { TvWelcomeScreen() }
            composable<TvSubscribe> { TvSignupScreensFlow() }
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
            composable<HelpSettings> { TvHelpScreen() }
            composable<About> { TvAboutScreen(licences) }
            composable<GeneralSettings> { TvGeneralSettingsScreen() }
            composable<ProtocolSettings> { TvProtocolSettingsScreen() }
            composable<NetworkSettings> { TvNetworkSettingsScreen() }
            composable<PrivacySettings> { TvPrivacySettingsScreen() }
            composable<ConnectionStats> { TvConnectionStatsScreen() }
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
            composable<Connection> { ConnectionScreen() }
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
            composable<AccountDeleted> { AccountDeletedScreen() }
            composable<Update> { UpdateScreen() }
            composable<GeneralSettings> { GeneralSettingsScreen() }
            composable<ProtocolSettings> { ProtocolSettingsScreen() }
            composable<NetworkSettings> { NetworkSettingsScreen() }
            composable<PrivacySettings> { PrivacySettingsScreen() }
            composable<HelpSettings> { HelpScreen() }
            composable<ObfuscationSettings> { ObfuscationSettingsScreen() }
            composable<DebugLogs> { VpnLogScreen() }
            composable<ConnectionStats> { ConnectionStatsScreen() }
            composable<ExternalAppList> { ExternalProxyAppList() }
        }
    }
    // endregion
}