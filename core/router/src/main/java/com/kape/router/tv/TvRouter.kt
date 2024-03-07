package com.kape.router.tv

import com.kape.router.AppFlow
import com.kape.router.Back
import com.kape.router.Connection
import com.kape.router.EnterFlow
import com.kape.router.Exit
import com.kape.router.ExitFlow
import com.kape.router.Login
import com.kape.router.NavigateBack
import com.kape.router.NavigateOut
import com.kape.router.Permissions
import com.kape.router.Router
import com.kape.router.Splash
import com.kape.router.TvLogin
import com.kape.router.TvWelcome
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TvRouter : Router {

    private val navigation = MutableStateFlow(Splash.Main)
    override fun handleFlow(flow: AppFlow) {
        when (flow) {
            is EnterFlow -> handleEnterFlow(flow)
            is ExitFlow -> handleExitFlow(flow)
            is Back -> handleBack()
            is Exit -> exitApp()
        }
    }

    override fun resetNavigation() {
        navigation.value = Splash.Main
    }

    override fun getNavigation(): StateFlow<String> =
        navigation

    private fun handleEnterFlow(flow: EnterFlow) {
        when (flow) {
            EnterFlow.Login -> navigation.value = Login.WithCredentials
            EnterFlow.Permissions -> navigation.value = Permissions.Route
            EnterFlow.Splash -> navigation.value = Splash.Main
            EnterFlow.Connection -> navigation.value = Connection.Main
            EnterFlow.VpnRegionSelection -> TODO("To be implemented")
            EnterFlow.ShadowsocksRegionSelection -> TODO("To be implemented")
            EnterFlow.Profile -> TODO("To be implemented")
            EnterFlow.Subscribe -> TODO("To be implemented")
            EnterFlow.PrivacyPolicy -> TODO("To be implemented")
            EnterFlow.TermsOfService -> TODO("To be implemented")
            EnterFlow.Settings -> TODO("To be implemented")
            EnterFlow.AutomationSettings -> TODO("To be implemented")
            EnterFlow.PerAppSettings -> TODO("To be implemented")
            EnterFlow.KillSwitchSettings -> TODO("To be implemented")
            EnterFlow.DedicatedIp -> TODO("To be implemented")
            EnterFlow.Support -> TODO("To be implemented")
            EnterFlow.Automation -> TODO("To be implemented")
            EnterFlow.ProtocolSettings -> TODO("To be implemented")
            EnterFlow.About -> TODO("To be implemented")
            EnterFlow.Customization -> TODO("To be implemented")
            EnterFlow.TvWelcome -> navigation.value = TvWelcome.Main
            EnterFlow.TvLoginUsername -> navigation.value = TvLogin.Username
        }
    }

    private fun handleExitFlow(flow: ExitFlow) {
        when (flow) {
            ExitFlow.Login -> handleEnterFlow(EnterFlow.Permissions)
            ExitFlow.Splash -> handleEnterFlow(EnterFlow.TvWelcome)
            ExitFlow.Connection -> TODO("To be implemented")
            ExitFlow.RegionSelection -> TODO("To be implemented")
            ExitFlow.Profile -> TODO("To be implemented")
            ExitFlow.Subscribe -> TODO("To be implemented")
            ExitFlow.Settings -> TODO("To be implemented")
            ExitFlow.PerAppSettings -> TODO("To be implemented")
            ExitFlow.DedicatedIp -> TODO("To be implemented")
            ExitFlow.AutomationSettings -> TODO("To be implemented")
            ExitFlow.KillSwitchSettings -> TODO("To be implemented")
            ExitFlow.Permissions -> handleEnterFlow(EnterFlow.Connection)
            ExitFlow.Automation -> TODO("To be implemented")
            ExitFlow.ProtocolSettings -> TODO("To be implemented")
            ExitFlow.About -> TODO("To be implemented")
            ExitFlow.Customization -> TODO("To be implemented")
        }
    }

    private fun handleBack() {
        navigation.value = NavigateBack
    }

    private fun exitApp() {
        navigation.value = NavigateOut
    }
}