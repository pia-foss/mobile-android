package tests

import androidx.test.uiautomator.uiAutomator
import org.junit.Test
import screens.helpers.UiAutomatorHelper.findByStartsWith
import screens.helpers.UiAutomatorHelper.waitUntilConnectionIsEstablished
import screens.steps.RegionSelectionSteps.searchAndConnectToRegion
import kotlin.test.assertNotNull

class UiRegionListTests : UiTest() {
    @Test
    fun valid_region_displays_in_list() =
        uiAutomator {
            val region = "Moldova"
            searchAndConnectToRegion(region)

            waitUntilConnectionIsEstablished()
            assertNotNull(findByStartsWith("Protected"))
        }
}