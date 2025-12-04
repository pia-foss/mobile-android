package screens.steps

import screens.helpers.UiAutomatorHelpers
import screens.helpers.UiAutomatorHelpers.waitUntilVisible

object RegionSelectionSteps {

    val searchBar get() = UiAutomatorHelpers.findByResId(":VpnRegionSelectionScreen:searchBar")
    val searchLocationText get() = UiAutomatorHelpers.findByResId(":VpnRegionSelectionScreen:locationItem_0:regionName")

    fun searchAndConnectToRegion(region: String) {
        UiAutomatorHelpers.click(MainScreenSteps.locationPicker)
        searchBar?.let { UiAutomatorHelpers.inputText(it, region) }
        waitUntilVisible(searchLocationText)
        UiAutomatorHelpers.click(searchLocationText)
        UiAutomatorHelpers.waitUntilConnectionIsEstablished()
    }
}
