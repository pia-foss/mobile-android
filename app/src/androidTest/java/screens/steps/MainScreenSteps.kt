package screens.steps

import screens.helpers.UiAutomatorHelpers
import screens.helpers.UiAutomatorHelpers.waitUntilVisible
import screens.helpers.UiAutomatorHelpers.waitUntilConnectionIsEstablished

object MainScreenSteps {

    val connectButton get() = UiAutomatorHelpers.findByResId(":ConnectionScreen:connection_button")
    val sideMenu get() = UiAutomatorHelpers.findByResId(":AppBar:side_menu")
    val appBarConnectionStatus get() = UiAutomatorHelpers.findByResId(":AppBar:connection_text_default")
    val quickConnectFirstItem get() = UiAutomatorHelpers.findByResId(":QuickConnect:server_0")
    val quickConnectSecondItem get() = UiAutomatorHelpers.findByResId(":QuickConnect:server_1")
    val locationPicker get() = UiAutomatorHelpers.findByResId(":ConnectionScreen:VpnLocationPicker")
    val locationEightItem get() = UiAutomatorHelpers.findByResId(":VpnRegionSelectionScreen:locationItem_8")

    fun navigateToMainScreen() {
        waitUntilVisible(connectButton)
    }

    fun establishAndVerifyVPNConnection() {
        UiAutomatorHelpers.click(connectButton)
        waitUntilConnectionIsEstablished()
    }
}
