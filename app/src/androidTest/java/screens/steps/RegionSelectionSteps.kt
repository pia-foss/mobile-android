package screens.steps

import androidx.test.uiautomator.UiAutomatorTestScope
import screens.helpers.UiAutomatorHelper.findByResId
import screens.helpers.UiAutomatorHelper.inputText
import screens.helpers.UiAutomatorHelper.waitUntilConnectionIsEstablished

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