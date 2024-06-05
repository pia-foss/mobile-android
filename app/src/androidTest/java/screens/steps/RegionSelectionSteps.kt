package screens.steps

import screens.helpers.UiAutomatorObjectFinder
import screens.helpers.UiAutomatorStepsHelper
import screens.helpers.UiAutomatorStepsHelper.waitUntilFound

object RegionSelectionSteps {
    val searchBar = UiAutomatorObjectFinder.findByResourceId(":VpnRegionSelectionScreen:searchBar")
    val searchLocationText = UiAutomatorObjectFinder.findByResourceId(":VpnRegionSelectionScreen:locationItem_0:regionName")

    fun searchAndConnectToRegion(region: String) {
        MainScreenSteps.locationPicker.clickAndWaitForNewWindow(UiAutomatorStepsHelper.defaultTimeout)
        UiAutomatorStepsHelper.inputTextInField(searchBar, region)
        waitUntilFound(searchLocationText)
        searchLocationText.click()
    }
}
