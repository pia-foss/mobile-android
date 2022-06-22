package com.kape.router

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class Router {

    private val _navigation = MutableStateFlow("")
    val navigation: StateFlow<String> = _navigation

    fun handleFlow(flow: AppFlow) {
        when (flow) {
            is EnterFlow -> handleEnterFlow(flow)
            is ExitFlow -> handleExitFlow(flow)
        }
    }

    private fun handleEnterFlow(flow: EnterFlow) {
        when (flow) {
            EnterFlow.Login -> _navigation.value = Login.Main
            EnterFlow.VpnPermission -> _navigation.value = VpnPermission.Main
            EnterFlow.Splash -> _navigation.value = Splash.Main
        }
    }

    private fun handleExitFlow(flow: ExitFlow) {
        when (flow) {
            ExitFlow.Login -> handleEnterFlow(EnterFlow.VpnPermission)
            ExitFlow.VpnPermission -> TODO()
            ExitFlow.Splash -> handleEnterFlow(EnterFlow.Login)
        }
    }
}