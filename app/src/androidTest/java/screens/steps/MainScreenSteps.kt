package screens.steps

import androidx.test.uiautomator.UiAutomatorTestScope
import screens.helpers.UiAutomatorHelper.CONNECTION_TIMEOUT
import screens.helpers.UiAutomatorHelper.LONG_TIMEOUT
import screens.helpers.UiAutomatorHelper.click
import screens.helpers.UiAutomatorHelper.waitUntilConnectionIsEstablished

object MainScreenSteps {
    const val CONNECT_BUTTON = ":ConnectionScreen:connection_button"
    const val SIDE_MENU = ":AppBar:side_menu"
    const val QUICK_CONNECT_FIRST_ITEM = ":QuickConnect:server_0"
    const val QUICK_CONNECT_SECOND_ITEM = ":QuickConnect:server_1"
    const val LOCATION_PICKER = ":ConnectionScreen:VpnLocationPicker"
    const val LOCATION_EIGHT_ITEM = ":VpnRegionSelectionScreen:locationItem_8"

    fun UiAutomatorTestScope.establishAndVerifyVPNConnection() {
        click(CONNECT_BUTTON, LONG_TIMEOUT)
        check(waitUntilConnectionIsEstablished(CONNECTION_TIMEOUT)) {
            "Vpn failed to connect within timeout"
        }
    }
}