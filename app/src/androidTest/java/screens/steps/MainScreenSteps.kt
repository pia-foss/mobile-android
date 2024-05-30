package screens.steps

import screens.helpers.UiAutomatorObjectFinder
import screens.helpers.UiAutomatorStepsHelper
import screens.helpers.UiAutomatorStepsHelper.waitUntilFound
import screens.helpers.UiAutomatorStepsHelper.waitUntilConnectionIsEstablished

object MainScreenSteps {
    val connectButton =
        UiAutomatorObjectFinder.findByResourceId(":ConnectionScreen:connection_button")
    val sideMenu =
        UiAutomatorObjectFinder.findByResourceId(":AppBar:side_menu")
    val appBarConnectionStatus = UiAutomatorObjectFinder.findByResourceId(":AppBar:connection_text_default")
    val quickConnectFirstItem = UiAutomatorObjectFinder.findByResourceId(":QuickConnect:server_0")
    val quickConnectSecondItem = UiAutomatorObjectFinder.findByResourceId(":QuickConnect:server_1")
    val locationPicker = UiAutomatorObjectFinder.findByResourceId(":ConnectionScreen:VpnLocationPicker")
    //we select 8 as it is most likely to have a different VPN server than the optimal location.
    val locationEightItem = UiAutomatorObjectFinder.findByResourceId(":VpnRegionSelectionScreen:locationItem_8")
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
        waitUntilConnectionIsEstablished()
    }
}
