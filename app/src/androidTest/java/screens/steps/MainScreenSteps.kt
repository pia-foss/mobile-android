package screens.steps

import screens.helpers.UiAutomatorObjectFinder
import screens.helpers.UiAutomatorStepsHelper
import screens.helpers.UiAutomatorStepsHelper.waitUntilFound
import screens.helpers.UiAutomatorStepsHelper.waitUntilVpnIpIsPopulated

object MainScreenSteps {
    val connectButton =
        UiAutomatorObjectFinder.findByResourceId(":ConnectionScreen:connection_button")
    val sideMenu =
        UiAutomatorObjectFinder.findByResourceId(":AppBar:side_menu")
    val appBarConnectionStatus = UiAutomatorObjectFinder.findByResourceId(":AppBar:connection_text_default")
    val vpnIp = UiAutomatorObjectFinder.findByResourceId(":Text:vpnIp")
    val quickConnectServerText = UiAutomatorObjectFinder.findByResourceId(":QuickConnect:serverText")
    val quickConnectServer = UiAutomatorObjectFinder.findByResourceId(":QuickConnect:Server")

    fun navigateToMainScreen() {
        try {
            waitUntilFound(connectButton)
        } catch (e: Exception) {
            UiAutomatorStepsHelper.device.pressBack()
            navigateToMainScreen()
        }
    }

    fun establishAndVerifyVPNConnection() {
        connectButton.click()
        val ipRegEx = "^((\\d{1,3})\\.){3}(\\d{1,3})\$"
        waitUntilVpnIpIsPopulated(":Text:vpnIp", ipRegEx)
    }
}
