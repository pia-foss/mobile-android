package com.kape.router.tv

import com.kape.router.About
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
import com.kape.router.Update
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
            EnterFlow.Profile -> navigation.value = Profile.Main
            EnterFlow.Subscribe -> navigation.value = Subscribe.Main
            EnterFlow.PrivacyPolicy -> navigation.value = WebContent.Privacy
            EnterFlow.TermsOfService -> navigation.value = WebContent.Terms
            EnterFlow.Settings -> navigation.value = Settings.Route
            EnterFlow.PerAppSettings -> navigation.value = PerAppSettings.Main
            EnterFlow.DedicatedIpActivate -> navigation.value = DedicatedIp.ActivateToken
            EnterFlow.About -> navigation.value = About.Main
            EnterFlow.TvWelcome -> navigation.value = TvWelcome.Main
            EnterFlow.TvLoginUsername -> navigation.value = TvLogin.Username
            EnterFlow.TvSideMenu -> navigation.value = TvSideMenu.Main
            EnterFlow.TvHelp -> navigation.value = TvHelp.Main
            EnterFlow.Update -> navigation.value = Update.Route
            EnterFlow.ShadowsocksRegionSelection,
            EnterFlow.AutomationSettings,
            EnterFlow.KillSwitchSettings,
            EnterFlow.Support,
            EnterFlow.Automation,
            EnterFlow.ProtocolSettings,
            EnterFlow.Customization,
            EnterFlow.AccountDeleted,
            EnterFlow.DedicatedIpPlans,
            -> throw IllegalStateException("Unsupported on TV")
            EnterFlow.NoInAppRegistration -> TODO()
        }
    }

    private fun handleExitFlow(flow: ExitFlow) {
        when (flow) {
            ExitFlow.Login -> handleEnterFlow(EnterFlow.Permissions)
            ExitFlow.Splash -> handleEnterFlow(EnterFlow.TvWelcome)
            ExitFlow.RegionSelection -> handleBack()
            ExitFlow.Subscribe -> handleEnterFlow(EnterFlow.Permissions)
            ExitFlow.Settings -> handleEnterFlow(EnterFlow.Connection)
            ExitFlow.DedicatedIp -> handleEnterFlow(EnterFlow.TvSideMenu)
            ExitFlow.Permissions -> handleEnterFlow(EnterFlow.Connection)
            ExitFlow.NoInAppRegistration -> TODO()
            ExitFlow.Connection,
            ExitFlow.Profile,
            ExitFlow.PerAppSettings,
            ExitFlow.AutomationSettings,
            ExitFlow.KillSwitchSettings,
            ExitFlow.Automation,
            ExitFlow.ProtocolSettings,
            ExitFlow.About,
            ExitFlow.Customization,
            ExitFlow.AccountDeleted,
            -> throw IllegalStateException("Unsupported on TV")
        }
    }

    private fun handleBack() {
        navigation.value = NavigateBack
    }

    private fun exitApp() {
        navigation.value = NavigateOut
    }
}