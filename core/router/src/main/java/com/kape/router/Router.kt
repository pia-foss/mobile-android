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
            EnterFlow.AutomationSettings -> _navigation.value = Settings.Automation
            EnterFlow.HelpSettings -> _navigation.value = Settings.Help
            EnterFlow.PerAppSettings -> _navigation.value = PerAppSettings.Main
            EnterFlow.KillSwitchSettings -> _navigation.value = Settings.KillSwitch
            EnterFlow.QuickSettings -> _navigation.value = Settings.QuickSettings
            EnterFlow.ConnectionStats -> _navigation.value = Settings.ConnectionStats
            EnterFlow.DebugLogs -> _navigation.value = Settings.DebugLogs
            EnterFlow.DedicatedIp -> _navigation.value = DedicatedIp.Main
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
            ExitFlow.AutomationSettings -> handleEnterFlow(EnterFlow.Settings)
            ExitFlow.HelpSettings -> handleEnterFlow(EnterFlow.Settings)
            ExitFlow.PerAppSettings -> handleEnterFlow(EnterFlow.Connection)
            ExitFlow.KillSwitchSettings -> handleEnterFlow(EnterFlow.PrivacySettings)
            ExitFlow.QuickSettings -> handleEnterFlow(EnterFlow.Connection)
            ExitFlow.ConnectionStats -> handleEnterFlow(EnterFlow.HelpSettings)
            ExitFlow.DebugLogs -> handleEnterFlow(EnterFlow.HelpSettings)
            ExitFlow.DedicatedIp -> handleEnterFlow(EnterFlow.Connection)
        }
    }

    private fun handleBack() {
        _navigation.value = NavigateBack
    }
}