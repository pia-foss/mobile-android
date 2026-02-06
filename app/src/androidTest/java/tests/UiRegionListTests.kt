package tests

import androidx.test.uiautomator.uiAutomator
import org.junit.Test
import screens.helpers.UiAutomatorHelpers
import screens.helpers.UiAutomatorHelpers.findByResId
import screens.helpers.UiAutomatorHelpers.findByStartsWith
import screens.helpers.UiAutomatorHelpers.waitUntilConnectionIsEstablished
import screens.steps.RegionSelectionSteps.searchAndConnectToRegion
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class UiRegionListTests : UiTest() {

    @Test
    fun valid_region_displays_in_list() = uiAutomator {
        val region = "Moldova"
        searchAndConnectToRegion(region)

        waitUntilConnectionIsEstablished()
        assertNotNull(findByStartsWith("Protected"))
    }
}
