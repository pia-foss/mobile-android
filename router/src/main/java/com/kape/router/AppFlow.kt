package com.kape.router

interface AppFlow

sealed class EnterFlow : AppFlow {
    object Login : EnterFlow()
    object VpnPermission : EnterFlow()
    object Splash : EnterFlow()
    object Connection : EnterFlow()
    object RegionSelection: EnterFlow()
}

sealed class ExitFlow : AppFlow {
    object Login : ExitFlow()
    object VpnPermission : ExitFlow()
    object Splash : ExitFlow()
    object Connection : ExitFlow()
    object RegionSelection: ExitFlow()
}