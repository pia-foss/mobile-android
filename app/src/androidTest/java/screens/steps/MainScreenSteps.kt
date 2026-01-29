package screens.steps

import androidx.test.uiautomator.UiAutomatorTestScope
import screens.helpers.UiAutomatorHelpers.get
import screens.helpers.UiAutomatorHelpers.waitForElement
import screens.helpers.UiAutomatorHelpers.waitUntilConnectionIsEstablished

object MainScreenSteps {

    const val CONNECT_BUTTON = ":ConnectionScreen:connection_button"
    const val SIDE_MENU = ":AppBar:side_menu"
    const val APPBAR_CONNECTION_STATUS = ":AppBar:connection_text_default"

    const val QUICK_CONNECT_FIRST_ITEM = ":QuickConnect:server_0"
    const val QUICK_CONNECT_SECOND_ITEM = ":QuickConnect:server_1"

    const val LOCATION_PICKER = ":ConnectionScreen:VpnLocationPicker"
    const val LOCATION_EIGHT_ITEM = ":VpnRegionSelectionScreen:locationItem_8"

    fun UiAutomatorTestScope.establishAndVerifyVPNConnection() {
        if (waitForElement(CONNECT_BUTTON)) {
            get(CONNECT_BUTTON).click()
            waitUntilConnectionIsEstablished()
        }
    }
}
