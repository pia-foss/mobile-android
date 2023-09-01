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
            EnterFlow.GeneralSettings -> _navigation.value = Settings.General
            EnterFlow.ProtocolSettings -> _navigation.value = Settings.Protocols
            EnterFlow.NetworkSettings -> _navigation.value = Settings.Networks
            EnterFlow.PrivacySettings -> _navigation.value = Settings.Privacy
        }
    }

    private fun handleExitFlow(flow: ExitFlow) {
        when (flow) {
            ExitFlow.Login -> handleEnterFlow(EnterFlow.VpnPermission)
            ExitFlow.VpnPermission -> handleEnterFlow(EnterFlow.Connection)
            ExitFlow.Splash -> handleEnterFlow(EnterFlow.Subscribe)
            ExitFlow.Connection -> TODO()
            ExitFlow.RegionSelection -> _navigation.value = NavigateBack
            ExitFlow.Profile -> TODO()
            ExitFlow.Subscribe -> handleEnterFlow(EnterFlow.VpnPermission)
            ExitFlow.Settings -> handleEnterFlow(EnterFlow.Connection)
            ExitFlow.GeneralSettings -> handleEnterFlow(EnterFlow.Settings)
            ExitFlow.ProtocolSettings -> handleEnterFlow(EnterFlow.GeneralSettings)
            ExitFlow.NetworkSettings -> handleEnterFlow(EnterFlow.Settings)
            ExitFlow.PrivacySettings -> handleEnterFlow(EnterFlow.Settings)
        }
    }

    private fun handleBack() {
        _navigation.value = NavigateBack
    }
}