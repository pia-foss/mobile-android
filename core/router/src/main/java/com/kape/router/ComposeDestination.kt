package com.kape.router

import kotlinx.serialization.Serializable

interface ComposeDestination

// Mobile screens

@Serializable
object Splash : ComposeDestination

@Serializable
object LoginWithCredentials : ComposeDestination

@Serializable
object LoginWithEmail : ComposeDestination

@Serializable
object VpnPermission : ComposeDestination

@Serializable
object NotificationPermission : ComposeDestination

@Serializable
object Connection : ComposeDestination

@Serializable
object VpnRegionSelection : ComposeDestination

@Serializable
object ShadowsocksRegionSelection : ComposeDestination

@Serializable
object Profile : ComposeDestination

@Serializable
object Subscribe : ComposeDestination

@Serializable
object Settings : ComposeDestination

@Serializable
object GeneralSettings : ComposeDestination

@Serializable
object ProtocolSettings : ComposeDestination

@Serializable
object AutomationSettings : ComposeDestination

@Serializable
object Automation: ComposeDestination

@Serializable
object KillSwitchSettings : ComposeDestination

@Serializable
object About : ComposeDestination

@Serializable
object PerAppSettings : ComposeDestination

@Serializable
object HelpSettings : ComposeDestination

@Serializable
object PrivacySettings : ComposeDestination

@Serializable
object NetworkSettings : ComposeDestination

@Serializable
object DedicatedIpActivateToken : ComposeDestination

@Serializable
object DedicatedIpSignupPlans : ComposeDestination

@Serializable
object DedicatedIpSignupTokenDetails: ComposeDestination

@Serializable
object DedicatedIpSignupTokenActivate: ComposeDestination

@Serializable
object DedicatedIpLocationSelection: ComposeDestination

@Serializable
object AutomationRoute : ComposeDestination

@Serializable
object AutomationLocation : ComposeDestination

@Serializable
object AutomationSet : ComposeDestination

@Serializable
object AutomationUpdate : ComposeDestination

@Serializable
object AutomationBackgroundLocation : ComposeDestination

@Serializable
object AutomationAddRule : ComposeDestination

@Serializable
object Customization : ComposeDestination

@Serializable
object AccountDeleted : ComposeDestination

@Serializable
object Update : ComposeDestination

@Serializable
object SideMenuRoute : ComposeDestination

@Serializable
object SideMenu : ComposeDestination

@Serializable
object ObfuscationSettings : ComposeDestination

@Serializable
object ExternalAppList : ComposeDestination

@Serializable
object DebugLogs : ComposeDestination

@Serializable
object ConnectionStats : ComposeDestination

@Serializable
sealed class WebDestination(val destination: String) : ComposeDestination {
    @Serializable
    object Terms : WebDestination("web-screen-terms")

    @Serializable
    object Privacy : WebDestination("web-screen-privacy")

    @Serializable
    object Support : WebDestination("web-screen-support")

    @Serializable
    object NoInAppRegistration : WebDestination("web-screen-registration")

    @Serializable
    object DeleteAccount : WebDestination("delete-account")
}

// TV screens
@Serializable
object TvLogin : ComposeDestination

@Serializable
object TvLoginUsername : ComposeDestination

@Serializable
object TvLoginPassword : ComposeDestination

@Serializable
object TvWelcome : ComposeDestination

@Serializable
object TvSideMenu : ComposeDestination

@Serializable
object TvHelp : ComposeDestination

@Serializable
object TvConnectionStats : ComposeDestination

@Serializable
object TvGeneralSettings : ComposeDestination

@Serializable
object TvNetworkSettings : ComposeDestination

@Serializable
object TvPerAppSettings : ComposeDestination

@Serializable
object TvPrivacySettings : ComposeDestination

@Serializable
object TvProtocolSettings : ComposeDestination

@Serializable
object TvSettings : ComposeDestination

@Serializable
object TvVpnPermission : ComposeDestination

@Serializable
object TvNotificationPermission : ComposeDestination

@Serializable
object TvConnect : ComposeDestination