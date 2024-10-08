package com.kape.router.mobile

import com.kape.router.About
import com.kape.router.AccountDeleted
import com.kape.router.AppFlow
import com.kape.router.Automation
import com.kape.router.Back
import com.kape.router.Connection
import com.kape.router.Customization
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
import com.kape.router.ShadowsocksRegionSelection
import com.kape.router.Splash
import com.kape.router.Subscribe
import com.kape.router.Update
import com.kape.router.VpnRegionSelection
import com.kape.router.WebContent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MobileRouter : Router {

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
            EnterFlow.Login -> navigation.value = Login.Route
            EnterFlow.Permissions -> navigation.value = Permissions.Route
            EnterFlow.Splash -> navigation.value = Splash.Main
            EnterFlow.Connection -> navigation.value = Connection.Main
            EnterFlow.VpnRegionSelection -> navigation.value = VpnRegionSelection.Main
            EnterFlow.ShadowsocksRegionSelection ->
                navigation.value =
                    ShadowsocksRegionSelection.Main

            EnterFlow.Profile -> navigation.value = Profile.Main
            EnterFlow.Subscribe -> navigation.value = Subscribe.Main
            EnterFlow.PrivacyPolicy -> navigation.value = WebContent.Privacy
            EnterFlow.TermsOfService -> navigation.value = WebContent.Terms
            EnterFlow.DeleteAccount -> navigation.value = WebContent.DeleteAccount
            EnterFlow.Settings -> navigation.value = Settings.Route
            EnterFlow.AutomationSettings -> navigation.value = Settings.Automation
            EnterFlow.PerAppSettings -> navigation.value = PerAppSettings.Main
            EnterFlow.KillSwitchSettings -> navigation.value = Settings.KillSwitch
            EnterFlow.DedicatedIpActivate -> navigation.value = DedicatedIp.ActivateToken
            EnterFlow.DedicatedIpPlans -> navigation.value = DedicatedIp.SignupPlans
            EnterFlow.Support -> navigation.value = WebContent.Support
            EnterFlow.Automation -> navigation.value = Automation.Route
            EnterFlow.ProtocolSettings -> navigation.value = Settings.Protocols
            EnterFlow.About -> navigation.value = About.Main
            EnterFlow.Customization -> navigation.value = Customization.Route
            EnterFlow.AccountDeleted -> navigation.value = AccountDeleted.Route
            EnterFlow.NoInAppRegistration -> navigation.value = WebContent.NoInAppRegistration
            EnterFlow.Update -> navigation.value = Update.Route
            EnterFlow.TvWelcome,
            EnterFlow.TvLoginUsername,
            EnterFlow.TvSideMenu,
            EnterFlow.TvHelp,
            -> throw IllegalStateException("Unsupported on Mobile")
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
            ExitFlow.DedicatedIp -> handleEnterFlow(EnterFlow.Connection)
            ExitFlow.AutomationSettings -> handleFlow(EnterFlow.Settings)
            ExitFlow.KillSwitchSettings -> handleFlow(EnterFlow.Settings)
            ExitFlow.Permissions -> handleEnterFlow(EnterFlow.Connection)
            ExitFlow.Automation -> handleEnterFlow(EnterFlow.Settings)
            ExitFlow.ProtocolSettings -> handleEnterFlow(EnterFlow.Connection)
            ExitFlow.About -> handleEnterFlow(EnterFlow.Connection)
            ExitFlow.Customization -> handleEnterFlow(EnterFlow.Connection)
            ExitFlow.AccountDeleted -> handleEnterFlow(EnterFlow.Splash)
            ExitFlow.NoInAppRegistration -> handleEnterFlow(EnterFlow.Subscribe)
        }
    }

    private fun handleBack() {
        navigation.value = NavigateBack
    }

    private fun exitApp() {
        navigation.value = NavigateOut
    }
}