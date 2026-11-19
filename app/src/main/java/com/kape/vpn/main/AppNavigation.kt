package com.kape.vpn.main

import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import com.kape.about.screens.mobile.AboutScreen
import com.kape.about.screens.tv.TvAboutScreen
import com.kape.automation.ui.screens.AddNewRuleScreen
import com.kape.automation.ui.screens.AutomationScreen
import com.kape.automation.ui.screens.AutomationSettingsScreen
import com.kape.automation.ui.screens.BackgroundLocationPermissionScreen
import com.kape.automation.ui.screens.LocationPermissionScreen
import com.kape.connection.customization.CustomizationScreen
import com.kape.connection.ui.mobile.ConnectionScreen
import com.kape.connection.ui.tv.TvConnectionScreen
import com.kape.data.About
import com.kape.data.AccountDeleted
import com.kape.data.AutomationAddRule
import com.kape.data.AutomationBackgroundLocation
import com.kape.data.AutomationLocation
import com.kape.data.AutomationMain
import com.kape.data.AutomationSettings
import com.kape.data.Connection
import com.kape.data.ConnectionStats
import com.kape.data.Consent
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
import com.kape.data.MockLocationSettings
import com.kape.data.NetworkSettings
import com.kape.data.NotificationPermission
import com.kape.data.ObfuscationSettings
import com.kape.data.PerAppSettings
import com.kape.data.PrivacySettings
import com.kape.data.Profile
import com.kape.data.ProtocolSettings
import com.kape.data.Settings
import com.kape.data.ShadowsocksRegionSelection
import com.kape.data.Splash
import com.kape.data.Subscribe
import com.kape.data.TvConsent
import com.kape.data.TvLoginPassword
import com.kape.data.TvLoginUsername
import com.kape.data.TvSideMenu
import com.kape.data.TvSubscribe
import com.kape.data.TvWelcome
import com.kape.data.TvWelcomeBack
import com.kape.data.Update
import com.kape.data.VpnPermission
import com.kape.data.VpnRegionSelection
import com.kape.data.WebDestination
import com.kape.data.WelcomeBack
import com.kape.dedicatedip.ui.screens.mobile.DedicatedIpScreen
import com.kape.dedicatedip.ui.screens.mobile.SignupDedicatedIpCountryScreen
import com.kape.dedicatedip.ui.screens.mobile.SignupDedicatedIpPurchaseSuccessScreen
import com.kape.dedicatedip.ui.screens.mobile.SignupDedicatedIpScreen
import com.kape.dedicatedip.ui.screens.mobile.SignupDedicatedIpTokenActivateScreen
import com.kape.dedicatedip.ui.screens.mobile.SignupDedicatedIpTokenDetailsScreen
import com.kape.dedicatedip.ui.screens.tv.TvDedicatedIpScreen
import com.kape.inappbrowser.ui.InAppBrowser
import com.kape.login.ui.mobile.LoginScreen
import com.kape.login.ui.mobile.LoginWithEmailScreen
import com.kape.login.ui.tv.LoginPasswordScreen
import com.kape.login.ui.tv.LoginUsernameScreen
import com.kape.obfuscationregionselection.ui.ShadowsocksRegionSelectionScreen
import com.kape.permissions.ui.mobile.NotificationPermissionScreen
import com.kape.permissions.ui.mobile.VpnPermissionScreen
import com.kape.permissions.ui.tv.TvNotificationPermissionScreen
import com.kape.permissions.ui.tv.TvVpnPermissionScreen
import com.kape.profile.ui.mobile.AccountDeletedScreen
import com.kape.profile.ui.mobile.ProfileScreen
import com.kape.profile.ui.tv.TvProfileScreen
import com.kape.settings.ui.screens.mobile.ConnectionStatsScreen
import com.kape.settings.ui.screens.mobile.ExternalProxyAppList
import com.kape.settings.ui.screens.mobile.GeneralSettingsScreen
import com.kape.settings.ui.screens.mobile.HelpScreen
import com.kape.settings.ui.screens.mobile.KillSwitchSettingScreen
import com.kape.settings.ui.screens.mobile.MockLocationSettingScreen
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
import com.kape.settings.ui.screens.tv.TvMockLocationSettingScreen
import com.kape.settings.ui.screens.tv.TvNetworkSettingsScreen
import com.kape.settings.ui.screens.tv.TvPerAppSettingsScreen
import com.kape.settings.ui.screens.tv.TvPrivacySettingsScreen
import com.kape.settings.ui.screens.tv.TvProtocolSettingsScreen
import com.kape.settings.ui.screens.tv.TvSettingsScreen
import com.kape.sidemenu.ui.mobile.SideMenuContent
import com.kape.sidemenu.ui.tv.TvSideMenuScreen
import com.kape.signup.ui.mobile.ConsentScreen
import com.kape.signup.ui.mobile.SignupScreensFlow
import com.kape.signup.ui.mobile.WelcomeBackScreen
import com.kape.signup.ui.tv.TvConsentScreen
import com.kape.signup.ui.tv.TvSignupScreensFlow
import com.kape.signup.ui.tv.TvWelcomeBackScreen
import com.kape.splash.SplashScreen
import com.kape.splash.ui.UpdateScreen
import com.kape.tvwelcome.ui.TvWelcomeScreen
import com.kape.vpnregionselection.ui.mobile.VpnRegionSelectionScreen
import com.kape.vpnregionselection.ui.tv.TvVpnRegionSelectionScreen
import com.kape.ui.R as UiR

fun appEntryProvider(isTv: Boolean): (NavKey) -> NavEntry<NavKey> =
    entryProvider(fallback = { destination -> NavEntry(destination) { UnsupportedDestinationScreen(it) } }) {
        if (isTv) tvEntries() else mobileEntries()
    }

private fun EntryProviderScope<NavKey>.tvEntries() {
    entry<Splash> { SplashScreen() }
    entry<Update> { UpdateScreen() }
    entry<TvLoginUsername> { LoginUsernameScreen() }
    entry<TvLoginPassword> { LoginPasswordScreen(it.username) }
    entry<TvConsent> { TvConsentScreen(it.isFirstScreen) }
    entry<TvWelcomeBack> { TvWelcomeBackScreen() }
    entry<TvSubscribe> { TvSignupScreensFlow() }
    entry<DedicatedIpActivateToken> { TvDedicatedIpScreen() }
    entry<TvWelcome> { TvWelcomeScreen() }
    entry<VpnPermission> { TvVpnPermissionScreen() }
    entry<NotificationPermission> { TvNotificationPermissionScreen() }
    entry<About> { TvAboutScreen() }
    entry<VpnRegionSelection> { TvVpnRegionSelectionScreen() }
    entry<Profile> { TvProfileScreen() }
    entry<Connection> { TvConnectionScreen() }
    entry<Settings> { TvSettingsScreen() }
    entry<PerAppSettings> { TvPerAppSettingsScreen() }
    entry<HelpSettings> { TvHelpScreen() }
    entry<GeneralSettings> { TvGeneralSettingsScreen() }
    entry<ProtocolSettings> { TvProtocolSettingsScreen() }
    entry<NetworkSettings> { TvNetworkSettingsScreen() }
    entry<PrivacySettings> { TvPrivacySettingsScreen() }
    entry<MockLocationSettings> { TvMockLocationSettingScreen() }
    entry<ConnectionStats> { TvConnectionStatsScreen() }
    entry<TvSideMenu> { TvSideMenuScreen() }
    entry<WebDestination.Terms> { InAppBrowser(stringResource(UiR.string.url_terms_of_service)) }
    entry<WebDestination.Privacy> { InAppBrowser(stringResource(UiR.string.url_privacy_policy)) }
}

private fun EntryProviderScope<NavKey>.mobileEntries() {
    entry<Splash> { SplashScreen() }
    entry<Update> { UpdateScreen() }
    entry<LoginWithCredentials> { LoginScreen() }
    entry<LoginWithEmail> { LoginWithEmailScreen() }
    entry<Consent> { ConsentScreen(it.isFirstScreen) }
    entry<WelcomeBack> { WelcomeBackScreen() }
    entry<Subscribe> { SignupScreensFlow() }
    entry<DedicatedIpActivateToken> { DedicatedIpScreen() }
    entry<DedicatedIpSignupPlans> { SignupDedicatedIpScreen() }
    entry<DedicatedIpSignupTokenDetails> { SignupDedicatedIpTokenDetailsScreen() }
    entry<DedicatedIpLocationSelection> { SignupDedicatedIpCountryScreen() }
    entry<DedicatedIpSignupTokenActivate> { SignupDedicatedIpTokenActivateScreen() }
    entry<DedicatedIpPurchaseSuccess> { SignupDedicatedIpPurchaseSuccessScreen() }
    entry<VpnPermission> { VpnPermissionScreen() }
    entry<NotificationPermission> { NotificationPermissionScreen() }
    entry<About> { AboutScreen() }
    entry<VpnRegionSelection> { VpnRegionSelectionScreen() }
    entry<ShadowsocksRegionSelection> { ShadowsocksRegionSelectionScreen() }
    entry<Profile> { ProfileScreen() }
    entry<Connection> { ConnectionScreen(drawerContent = { scope, state -> SideMenuContent(scope, state) }) }
    entry<Customization> { CustomizationScreen() }
    entry<Settings> { SettingsScreen() }
    entry<AutomationSettings> { AutomationSettingsScreen() }
    entry<PerAppSettings> { PerAppSettingsScreen() }
    entry<KillSwitchSettings> { KillSwitchSettingScreen() }
    entry<MockLocationSettings> { MockLocationSettingScreen() }
    entry<GeneralSettings> { GeneralSettingsScreen() }
    entry<ProtocolSettings> { ProtocolSettingsScreen() }
    entry<NetworkSettings> { NetworkSettingsScreen() }
    entry<PrivacySettings> { PrivacySettingsScreen() }
    entry<HelpSettings> { HelpScreen() }
    entry<ObfuscationSettings> { ObfuscationSettingsScreen() }
    entry<DebugLogs> { VpnLogScreen() }
    entry<ConnectionStats> { ConnectionStatsScreen() }
    entry<ExternalAppList> { ExternalProxyAppList() }
    entry<AutomationMain> { AutomationScreen() }
    entry<AutomationAddRule> { AddNewRuleScreen() }
    entry<AutomationLocation> { LocationPermissionScreen() }
    entry<AutomationBackgroundLocation> { BackgroundLocationPermissionScreen() }
    entry<AccountDeleted> { AccountDeletedScreen() }
    entry<WebDestination.Terms> { InAppBrowser(stringResource(UiR.string.url_terms_of_service)) }
    entry<WebDestination.Privacy> { InAppBrowser(stringResource(UiR.string.url_privacy_policy)) }
    entry<WebDestination.Support> { InAppBrowser(stringResource(UiR.string.url_support)) }
    entry<WebDestination.NoInAppRegistration> { InAppBrowser(stringResource(UiR.string.url_registration)) }
    entry<WebDestination.DeleteAccount> { InAppBrowser(stringResource(UiR.string.url_delete_account)) }
}