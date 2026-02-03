package screens.steps

import screens.helpers.UiAutomatorHelpers

object RegionSelectionSteps {

    val searchBar=":VpnRegionSelectionScreen:searchBar"
    val searchLocationText=":VpnRegionSelectionScreen:locationItem_0:regionName"

    fun searchAndConnectToRegion(region: String) {
        UiAutomatorHelpers.findByResId(MainScreenSteps.locationPicker)?.click()
        UiAutomatorHelpers.inputTextWhenReady(searchBar, region)
        UiAutomatorHelpers.findByResId(searchLocationText)?.click()
        UiAutomatorHelpers.waitUntilConnectionIsEstablished()
    }
}
