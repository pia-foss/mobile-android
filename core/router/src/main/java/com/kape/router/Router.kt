package com.kape.router

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class Router {

    private val _navigation = MutableStateFlow(Splash.Main)
    val navigation: StateFlow<String> = _navigation

    fun handleFlow(flow: AppFlow) {
        when (flow) {
            is EnterFlow -> handleEnterFlow(flow)
            is ExitFlow -> handleExitFlow(flow)
            is Back -> handleBack()
            is Exit -> exitApp()
        }
    }

    fun resetNavigation() {
        _navigation.value = Splash.Main
    }

    private fun handleEnterFlow(flow: EnterFlow) {
        when (flow) {
            EnterFlow.Login -> _navigation.value = Login.Route
            EnterFlow.Permissions -> _navigation.value = Permissions.Route
            EnterFlow.Splash -> _navigation.value = Splash.Main
            EnterFlow.Connection -> _navigation.value = Connection.Main
            EnterFlow.RegionSelection -> _navigation.value = RegionSelection.Main
            EnterFlow.Profile -> _navigation.value = Profile.Main
            EnterFlow.Subscribe -> _navigation.value = Subscribe.Main
            EnterFlow.PrivacyPolicy -> _navigation.value = WebContent.Privacy
            EnterFlow.TermsOfService -> _navigation.value = WebContent.Terms
            EnterFlow.Survey -> _navigation.value = WebContent.Survey
            EnterFlow.Settings -> _navigation.value = Settings.Route
            EnterFlow.AutomationSettings -> _navigation.value = Settings.Automation
            EnterFlow.PerAppSettings -> _navigation.value = PerAppSettings.Main
            EnterFlow.KillSwitchSettings -> _navigation.value = Settings.KillSwitch
            EnterFlow.QuickSettings -> _navigation.value = Settings.QuickSettings
            EnterFlow.DedicatedIp -> _navigation.value = DedicatedIp.Main
            EnterFlow.Support -> _navigation.value = WebContent.Support
            EnterFlow.Automation -> _navigation.value = Automation.Route
        }
    }

    private fun handleExitFlow(flow: ExitFlow) {
        when (flow) {
            ExitFlow.Login -> handleEnterFlow(EnterFlow.Permissions)
            ExitFlow.Splash -> handleEnterFlow(EnterFlow.Subscribe)
            ExitFlow.Connection -> exitApp()
            ExitFlow.RegionSelection -> handleBack()
            ExitFlow.Profile -> handleBack()
            ExitFlow.Subscribe -> handleEnterFlow(EnterFlow.Permissions)
            ExitFlow.Settings -> handleEnterFlow(EnterFlow.Connection)
            ExitFlow.PerAppSettings -> handleEnterFlow(EnterFlow.Connection)
            ExitFlow.QuickSettings -> handleEnterFlow(EnterFlow.Connection)
            ExitFlow.DedicatedIp -> handleEnterFlow(EnterFlow.Connection)
            ExitFlow.AutomationSettings -> handleFlow(EnterFlow.Settings)
            ExitFlow.KillSwitchSettings -> handleFlow(EnterFlow.Settings)
            ExitFlow.Permissions -> handleEnterFlow(EnterFlow.Connection)
            ExitFlow.Automation -> handleEnterFlow(EnterFlow.Settings)
        }
    }

    private fun handleBack() {
        _navigation.value = NavigateBack
    }

    private fun exitApp() {
        _navigation.value = NavigateOut
    }
}