package com.kape.router.tv

import com.kape.router.AppFlow
import com.kape.router.Back
import com.kape.router.Connection
import com.kape.router.DedicatedIp
import com.kape.router.Default
import com.kape.router.EnterFlow
import com.kape.router.Exit
import com.kape.router.ExitFlow
import com.kape.router.Login
import com.kape.router.NavigateBack
import com.kape.router.NavigateOut
import com.kape.router.PerAppSettings
import com.kape.router.Permissions
import com.kape.router.Profile
import com.kape.router.Router
import com.kape.router.Settings
import com.kape.router.Splash
import com.kape.router.Subscribe
import com.kape.router.TvHelp
import com.kape.router.TvLogin
import com.kape.router.TvSideMenu
import com.kape.router.TvWelcome
import com.kape.router.VpnRegionSelection
import com.kape.router.WebContent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TvRouter : Router {

    private val navigation = MutableStateFlow(Default.Route)
    override fun handleFlow(flow: AppFlow) {
        when (flow) {
            is EnterFlow -> handleEnterFlow(flow)
            is ExitFlow -> handleExitFlow(flow)
            is Back -> handleBack()
            is Exit -> exitApp()
        }
    }

    override fun resetNavigation() {
        navigation.value = Default.Route
    }

    override fun getNavigation(): StateFlow<String> =
        navigation

    private fun handleEnterFlow(flow: EnterFlow) {
        when (flow) {
            EnterFlow.Login -> navigation.value = Login.WithCredentials
            EnterFlow.Permissions -> navigation.value = Permissions.Route
            EnterFlow.Splash -> navigation.value = Splash.Main
            EnterFlow.Connection -> navigation.value = Connection.Main
            EnterFlow.VpnRegionSelection -> navigation.value = VpnRegionSelection.Main
            EnterFlow.ShadowsocksRegionSelection -> TODO("To be implemented")
            EnterFlow.Profile -> navigation.value = Profile.Main
            EnterFlow.Subscribe -> navigation.value = Subscribe.Main
            EnterFlow.PrivacyPolicy -> navigation.value = WebContent.Privacy
            EnterFlow.TermsOfService -> navigation.value = WebContent.Terms
            EnterFlow.Settings -> navigation.value = Settings.Route
            EnterFlow.AutomationSettings -> TODO("To be implemented")
            EnterFlow.PerAppSettings -> navigation.value = PerAppSettings.Main
            EnterFlow.KillSwitchSettings -> TODO("To be implemented")
            EnterFlow.DedicatedIp -> navigation.value = DedicatedIp.Main
            EnterFlow.Support -> TODO("To be implemented")
            EnterFlow.Automation -> TODO("To be implemented")
            EnterFlow.ProtocolSettings -> TODO("To be implemented")
            EnterFlow.About -> TODO("To be implemented")
            EnterFlow.Customization -> TODO("To be implemented")
            EnterFlow.TvWelcome -> navigation.value = TvWelcome.Main
            EnterFlow.TvLoginUsername -> navigation.value = TvLogin.Username
            EnterFlow.AccountDeleted -> TODO("To be implemented")
            EnterFlow.TvSideMenu -> navigation.value = TvSideMenu.Main
            EnterFlow.TvHelp -> navigation.value = TvHelp.Main
        }
    }

    private fun handleExitFlow(flow: ExitFlow) {
        when (flow) {
            ExitFlow.Login -> handleEnterFlow(EnterFlow.Permissions)
            ExitFlow.Splash -> handleEnterFlow(EnterFlow.TvWelcome)
            ExitFlow.Connection -> TODO("To be implemented")
            ExitFlow.RegionSelection -> handleBack()
            ExitFlow.Profile -> TODO("To be implemented")
            ExitFlow.Subscribe -> handleEnterFlow(EnterFlow.Permissions)
            ExitFlow.Settings -> TODO("To be implemented")
            ExitFlow.PerAppSettings -> TODO("To be implemented")
            ExitFlow.DedicatedIp -> handleEnterFlow(EnterFlow.TvSideMenu)
            ExitFlow.AutomationSettings -> TODO("To be implemented")
            ExitFlow.KillSwitchSettings -> TODO("To be implemented")
            ExitFlow.Permissions -> handleEnterFlow(EnterFlow.Connection)
            ExitFlow.Automation -> TODO("To be implemented")
            ExitFlow.ProtocolSettings -> TODO("To be implemented")
            ExitFlow.About -> TODO("To be implemented")
            ExitFlow.Customization -> TODO("To be implemented")
            ExitFlow.AccountDeleted -> TODO("To be implemented")
        }
    }

    private fun handleBack() {
        navigation.value = NavigateBack
    }

    private fun exitApp() {
        navigation.value = NavigateOut
    }
}