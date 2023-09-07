package com.kape.router

interface AppFlow

sealed class EnterFlow : AppFlow {
    data object Login : EnterFlow()
    data object VpnPermission : EnterFlow()
    data object Splash : EnterFlow()
    data object Connection : EnterFlow()
    data object RegionSelection : EnterFlow()
    data object Profile : EnterFlow()
    data object Subscribe : EnterFlow()
    data object TermsOfService : EnterFlow()
    data object PrivacyPolicy : EnterFlow()
    data object Survey : EnterFlow()
    data object Settings : EnterFlow()
    data object GeneralSettings : EnterFlow()
    data object ProtocolSettings : EnterFlow()
    data object NetworkSettings : EnterFlow()
    data object PrivacySettings : EnterFlow()
    data object AutomationSettings : EnterFlow()
    data object HelpSettings : EnterFlow()
    data object PerAppSettings : EnterFlow()
    data object KillSwitchSettings : EnterFlow()
}

sealed class ExitFlow : AppFlow {
    data object Login : ExitFlow()
    data object VpnPermission : ExitFlow()
    data object Splash : ExitFlow()
    data object Connection : ExitFlow()
    data object RegionSelection : ExitFlow()
    data object Profile : ExitFlow()
    data object Subscribe : ExitFlow()
    data object Settings : ExitFlow()
    data object GeneralSettings : ExitFlow()
    data object ProtocolSettings : ExitFlow()
    data object NetworkSettings : ExitFlow()
    data object PrivacySettings : ExitFlow()
    data object AutomationSettings : ExitFlow()
    data object HelpSettings : ExitFlow()
    data object PerAppSettings : ExitFlow()
    data object KillSwitchSettings : ExitFlow()
}

object Back : AppFlow