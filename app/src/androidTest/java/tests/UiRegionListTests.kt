package tests

import org.junit.Test
import screens.helpers.UiAutomatorStepsHelper
import kotlin.test.assertTrue

class UiRegionListTests : UiTest() {

    @Test
    fun valid_region_displays_in_list() {
        val region = "Moldova"
        regionSelectionSteps.searchAndConnectToRegion(region)
        UiAutomatorStepsHelper.waitUntilConnectionIsEstablished()
        val connectionText = mainScreenSteps.appBarConnectionStatus.text
        assertTrue(connectionText.equals("Connected to ${region}"))
    }
}
