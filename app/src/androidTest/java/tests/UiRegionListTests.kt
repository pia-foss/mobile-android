package tests

import org.junit.Test
import screens.helpers.UiAutomatorHelpers
import screens.helpers.UiAutomatorHelpers.waitUntilConnectionIsEstablished
import kotlin.test.assertTrue

class UiRegionListTests : UiTest() {

    @Test
    fun valid_region_displays_in_list() {
        val region = "Moldova"
        regionSelectionSteps.searchAndConnectToRegion(region)

        waitUntilConnectionIsEstablished()
        val connectionText =
            UiAutomatorHelpers.findByResId(mainScreenSteps.appBarConnectionStatus)!!.text
        assertTrue(connectionText.startsWith("Protected"))
    }
}
