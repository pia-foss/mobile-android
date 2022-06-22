package com.kape.router

interface AppFlow

sealed class EnterFlow : AppFlow {
    object Login : EnterFlow()
    object VpnPermission : EnterFlow()
    object Splash : EnterFlow()

}

sealed class ExitFlow : AppFlow {
    object Login : ExitFlow()
    object VpnPermission : ExitFlow()
    object Splash : ExitFlow()
}