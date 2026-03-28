package com.kape.contracts.data

import kotlinx.serialization.Serializable

interface ComposeDestination {
    val navOptions: DestinationNavOptions get() = DestinationNavOptions.None
}

sealed interface DestinationNavOptions {
    data object None : DestinationNavOptions
    data class PopUpTo(
        val destination: ComposeDestination,
        val inclusive: Boolean = true,
    ) : DestinationNavOptions

    data object ClearAll : DestinationNavOptions
}

// Mobile screens

@Serializable
object Splash : ComposeDestination {
    override val navOptions: DestinationNavOptions =
        DestinationNavOptions.ClearAll
}

@Serializable
object LoginWithCredentials : ComposeDestination

@Serializable
object LoginWithEmail : ComposeDestination

@Serializable
object VpnPermission : ComposeDestination {
    override val navOptions: DestinationNavOptions =
        DestinationNavOptions.PopUpTo(Splash, inclusive = true)
}

@Serializable
object NotificationPermission : ComposeDestination {
    override val navOptions: DestinationNavOptions =
        DestinationNavOptions.PopUpTo(Splash, inclusive = true)
}

@Serializable
object Connection : ComposeDestination {
    override val navOptions: DestinationNavOptions =
        DestinationNavOptions.ClearAll
}

@Serializable
object VpnRegionSelection : ComposeDestination

@Serializable
object ShadowsocksRegionSelection : ComposeDestination

@Serializable
object Profile : ComposeDestination

@Serializable
object Subscribe : ComposeDestination {
    override val navOptions: DestinationNavOptions =
        DestinationNavOptions.PopUpTo(Splash, inclusive = true)
}

@Serializable
object TvSubscribe : ComposeDestination {
}

@Serializable
object Settings : ComposeDestination

@Serializable
object GeneralSettings : ComposeDestination

@Serializable
object ProtocolSettings : ComposeDestination

@Serializable
object AutomationSettings : ComposeDestination

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
object DedicatedIpSignupTokenDetails : ComposeDestination

@Serializable
object DedicatedIpSignupTokenActivate : ComposeDestination

@Serializable
object DedicatedIpLocationSelection : ComposeDestination

@Serializable
object DedicatedIpPurchaseSuccess : ComposeDestination

@Serializable
object AutomationLocation : ComposeDestination {
    override val navOptions: DestinationNavOptions =
        DestinationNavOptions.PopUpTo(AutomationSettings, inclusive = false)
}

@Serializable
object AutomationMain : ComposeDestination {
    override val navOptions: DestinationNavOptions =
        DestinationNavOptions.PopUpTo(AutomationSettings, inclusive = false)
}

@Serializable
object AutomationBackgroundLocation : ComposeDestination {
    override val navOptions: DestinationNavOptions =
        DestinationNavOptions.PopUpTo(AutomationSettings, inclusive = false)
}

@Serializable
object AutomationAddRule : ComposeDestination

@Serializable
object Customization : ComposeDestination

@Serializable
object AccountDeleted : ComposeDestination {
    override val navOptions: DestinationNavOptions =
        DestinationNavOptions.PopUpTo(Splash, inclusive = true)
}

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
object TvWelcome : ComposeDestination {
    override val navOptions: DestinationNavOptions = DestinationNavOptions.ClearAll
}

@Serializable
object TvSideMenu : ComposeDestination

@Serializable
object TvVpnPermission : ComposeDestination {
    override val navOptions: DestinationNavOptions =
        DestinationNavOptions.PopUpTo(Splash, inclusive = true)
}

@Serializable
object TvNotificationPermission : ComposeDestination {
    override val navOptions: DestinationNavOptions =
        DestinationNavOptions.PopUpTo(Splash, inclusive = true)
}

@Serializable
object TvConnect : ComposeDestination