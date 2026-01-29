package tests

import androidx.test.uiautomator.uiAutomator
import org.junit.Test
import screens.helpers.UiAutomatorHelpers.get
import screens.helpers.UiAutomatorHelpers.launchAppAndLogIn
import screens.helpers.UiAutomatorHelpers.waitUntilConnectionIsEstablished
import screens.steps.MainScreenSteps
import screens.steps.RegionSelectionSteps.searchAndConnectToRegion
import kotlin.test.assertTrue

class UiRegionListTests {

    @Test
    fun valid_region_displays_in_list() = uiAutomator {
        launchAppAndLogIn()

        val region = "Moldova"
        searchAndConnectToRegion(region)

        waitUntilConnectionIsEstablished()
        val connectionText = get(MainScreenSteps.APPBAR_CONNECTION_STATUS).text
        assertTrue(connectionText.startsWith("Protected"))
    }
}
