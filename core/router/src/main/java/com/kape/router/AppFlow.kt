package com.kape.router

interface AppFlow

sealed class EnterFlow : AppFlow {
    data object Login : EnterFlow()
    data object Permissions : EnterFlow()
    data object Splash : EnterFlow()
    data object Connection : EnterFlow()
    data object VpnRegionSelection : EnterFlow()
    data object ShadowsocksRegionSelection : EnterFlow()
    data object Profile : EnterFlow()
    data object DeleteAccount : EnterFlow()
    data object Subscribe : EnterFlow()
    data object TermsOfService : EnterFlow()
    data object PrivacyPolicy : EnterFlow()
    data object Support : EnterFlow()
    data object NoInAppRegistration : EnterFlow()
    data object Settings : EnterFlow()
    data object ProtocolSettings : EnterFlow()
    data object AutomationSettings : EnterFlow()
    data object PerAppSettings : EnterFlow()
    data object KillSwitchSettings : EnterFlow()
    data object DedicatedIpActivate : EnterFlow()
    data object DedicatedIpPlans : EnterFlow()
    data object Automation : EnterFlow()
    data object About : EnterFlow()
    data object Customization : EnterFlow()
    data object AccountDeleted : EnterFlow()
    data object Update : EnterFlow()
    data object TvWelcome : EnterFlow()
    data object TvLoginUsername : EnterFlow()
    data object TvSideMenu : EnterFlow()
    data object TvHelp : EnterFlow()
}

sealed class ExitFlow : AppFlow {
    data object Login : ExitFlow()
    data object Permissions : ExitFlow()
    data object Splash : ExitFlow()
    data object Connection : ExitFlow()
    data object RegionSelection : ExitFlow()
    data object Profile : ExitFlow()
    data object Subscribe : ExitFlow()
    data object NoInAppRegistration : ExitFlow()
    data object Settings : ExitFlow()
    data object ProtocolSettings : ExitFlow()
    data object AutomationSettings : ExitFlow()
    data object PerAppSettings : ExitFlow()
    data object KillSwitchSettings : ExitFlow()
    data object DedicatedIp : ExitFlow()
    data object Automation : ExitFlow()
    data object About : ExitFlow()
    data object Customization : ExitFlow()
    data object AccountDeleted : ExitFlow()
}

object Back : AppFlow

object Exit : AppFlow