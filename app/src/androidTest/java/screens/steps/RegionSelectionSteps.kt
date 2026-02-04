package screens.steps

import androidx.test.uiautomator.UiAutomatorTestScope
import screens.helpers.UiAutomatorHelpers.findByResId
import screens.helpers.UiAutomatorHelpers.inputText
import screens.helpers.UiAutomatorHelpers.waitUntilConnectionIsEstablished

object RegionSelectionSteps {
    const val SEARCH_BAR = ":VpnRegionSelectionScreen:searchBar"
    const val SEARCH_LOCATION_TEXT = ":VpnRegionSelectionScreen:locationItem_0:regionName"

    fun UiAutomatorTestScope.searchAndConnectToRegion(region: String) {
        findByResId(MainScreenSteps.LOCATION_PICKER)?.click()
        inputText(SEARCH_BAR, region)
        findByResId(SEARCH_LOCATION_TEXT)?.click()
        waitUntilConnectionIsEstablished()
    }
}
