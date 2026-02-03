package screens.steps

import screens.helpers.UiAutomatorHelpers
import screens.helpers.UiAutomatorHelpers.waitUntilConnectionIsEstablished

object MainScreenSteps {

    val connectButton=":ConnectionScreen:connection_button"
    val sideMenu=":AppBar:side_menu"
    val appBarConnectionStatus=":AppBar:connection_text_default"
    val quickConnectFirstItem=":QuickConnect:server_0"
    val quickConnectSecondItem=":QuickConnect:server_1"
    val locationPicker=":ConnectionScreen:VpnLocationPicker"
    val locationEightItem=":VpnRegionSelectionScreen:locationItem_8"

    fun establishAndVerifyVPNConnection() {
        UiAutomatorHelpers.clickWhenReady(connectButton)
        waitUntilConnectionIsEstablished()
    }
}
