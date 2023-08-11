package com.kape.router

interface AppFlow

sealed class EnterFlow : AppFlow {
    object Login : EnterFlow()
    object VpnPermission : EnterFlow()
    object Splash : EnterFlow()
    object Connection : EnterFlow()
    object RegionSelection : EnterFlow()
    object Profile : EnterFlow()
    object Subscribe : EnterFlow()
    object TermsOfService : EnterFlow()
    object PrivacyPolicy : EnterFlow()
    object Survey : EnterFlow()
    object Settings: EnterFlow()
}

sealed class ExitFlow : AppFlow {
    object Login : ExitFlow()
    object VpnPermission : ExitFlow()
    object Splash : ExitFlow()
    object Connection : ExitFlow()
    object RegionSelection : ExitFlow()
    object Profile : ExitFlow()
    object Subscribe : ExitFlow()
    object Settings: ExitFlow()
}

object Back : AppFlow