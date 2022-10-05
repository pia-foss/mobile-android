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
            is Back -> handleBack()
        }
    }

    private fun handleEnterFlow(flow: EnterFlow) {
        when (flow) {
            EnterFlow.Login -> _navigation.value = Login.Main
            EnterFlow.VpnPermission -> _navigation.value = VpnPermission.Main
            EnterFlow.Splash -> _navigation.value = Splash.Main
            EnterFlow.Connection -> _navigation.value = Connection.Main
            EnterFlow.RegionSelection -> _navigation.value = RegionSelection.Main
            EnterFlow.Profile -> _navigation.value = Profile.Main
            EnterFlow.Subscribe -> _navigation.value = Subscribe.Main
            EnterFlow.PrivacyPolicy -> _navigation.value = WebContent.Privacy
            EnterFlow.TermsOfService -> _navigation.value = WebContent.Terms
            EnterFlow.Survey -> _navigation.value = WebContent.Survey
            EnterFlow.Settings -> _navigation.value = Settings.Main
        }
    }

    private fun handleExitFlow(flow: ExitFlow) {
        when (flow) {
            ExitFlow.Login -> handleEnterFlow(EnterFlow.VpnPermission)
            ExitFlow.VpnPermission -> {
                handleEnterFlow(EnterFlow.Connection)
            }
            ExitFlow.Splash -> handleEnterFlow(EnterFlow.Subscribe)
            ExitFlow.Connection -> TODO()
            ExitFlow.RegionSelection -> _navigation.value = NavigateBack
            ExitFlow.Profile -> TODO()
            ExitFlow.Subscribe -> handleEnterFlow(EnterFlow.VpnPermission)
        }
    }

    private fun handleBack() {
        _navigation.value = NavigateBack
    }
}