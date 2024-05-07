package screens.steps

import screens.helpers.UiAutomatorObjectFinder
import screens.helpers.UiAutomatorStepsHelper
import screens.helpers.UiAutomatorStepsHelper.waitUntilFound

object MainScreenSteps {
    val connectButton =
        UiAutomatorObjectFinder.findByResourceId(":ConnectionScreen:connection_button")
    val sideMenu =
        UiAutomatorObjectFinder.findByResourceId(":AppBar:side_menu")
    val vpnIp = UiAutomatorObjectFinder.findByResourceId(":IpInfo:vpnIp")
    val connectionStatus = UiAutomatorObjectFinder.findByResourceId(":AppBar:connection_status")
    val connectionText = UiAutomatorObjectFinder.findByResourceId(":AppBar:connection_text_default")
    val IPText = UiAutomatorObjectFinder.findByResourceId(":Text:IPText")

    fun navigateToMainScreen()
    {
        try {
            waitUntilFound(connectButton)
        } catch (e: Exception) {
            UiAutomatorStepsHelper.device.pressBack()
            navigateToMainScreen()
        }
    }

    fun connectToVPN() {
        connectButton.click()
        //I added this delay to wait for the vpn to connect and to show the vpn ip to show up in the UI.
        //however, i do feel that we don't really "wait" for the vpnip to show but instead
        //we check the value of the variable.
        Thread.sleep(5000L)
    }
}
