package tests

import org.junit.Test
import screens.helpers.UiAutomatorHelpers.waitUntilConnectionIsEstablished
import kotlin.test.assertTrue

class UiRegionListTests : UiTest() {

    @Test
    fun valid_region_displays_in_list() {
        val region = "Moldova"
        regionSelectionSteps.searchAndConnectToRegion(region)

        waitUntilConnectionIsEstablished()
        val connectionText = mainScreenSteps.appBarConnectionStatus!!.text
        assertTrue(connectionText.contains(region))
    }
}
