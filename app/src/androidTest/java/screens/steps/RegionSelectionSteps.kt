package screens.steps

import androidx.test.uiautomator.UiAutomatorTestScope
import screens.helpers.UiAutomatorHelpers.get
import screens.helpers.UiAutomatorHelpers.inputText
import screens.helpers.UiAutomatorHelpers.waitForElement
import screens.helpers.UiAutomatorHelpers.waitUntilConnectionIsEstablished

object RegionSelectionSteps {

    const val SEARCH_BAR = ":VpnRegionSelectionScreen:searchBar"
    const val SEARCH_LOCATION_TEXT = ":VpnRegionSelectionScreen:locationItem_0:regionName"

    fun UiAutomatorTestScope.searchAndConnectToRegion(region: String) {
        get(MainScreenSteps.LOCATION_PICKER).click()
        waitForElement(SEARCH_BAR)
        inputText(get(SEARCH_BAR), region)
        waitForElement(SEARCH_LOCATION_TEXT)
        get(SEARCH_LOCATION_TEXT).click()
        waitUntilConnectionIsEstablished()
    }
}
