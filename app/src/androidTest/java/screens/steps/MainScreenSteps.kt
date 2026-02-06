package screens.steps

import androidx.test.uiautomator.UiAutomatorTestScope
import screens.helpers.UiAutomatorHelpers.click
import screens.helpers.UiAutomatorHelpers.waitUntilConnectionIsEstablished

object MainScreenSteps {
    const val CONNECT_BUTTON = ":ConnectionScreen:connection_button"
    const val SIDE_MENU = ":AppBar:side_menu"
    const val QUICK_CONNECT_FIRST_ITEM = ":QuickConnect:server_0"
    const val QUICK_CONNECT_SECOND_ITEM = ":QuickConnect:server_1"
    const val LOCATION_PICKER = ":ConnectionScreen:VpnLocationPicker"
    const val LOCATION_EIGHT_ITEM = ":VpnRegionSelectionScreen:locationItem_8"

    fun UiAutomatorTestScope.establishAndVerifyVPNConnection() {
        click(CONNECT_BUTTON)
        waitUntilConnectionIsEstablished()
    }
}
