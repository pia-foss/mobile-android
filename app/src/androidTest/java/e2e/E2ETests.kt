package e2e

import androidx.test.uiautomator.uiAutomator
import org.junit.Test

class E2ETests {
    // Connection tests
    @PhoneTest
    @Test
    fun connectAfterLogin() =
        uiAutomator {
            login()
            onElement { viewIdResourceName == Main.CONNECT_BUTTON }.click()
            assertConnected()
        }

    @PhoneTest
    @Test
    fun searchAndConnectToRegion() =
        uiAutomator {
            login()
            onElement { viewIdResourceName == Main.LOCATION_PICKER }.click()
            onElement { viewIdResourceName == RegionSelection.SEARCH_BAR }.click()
            onElement { viewIdResourceName == RegionSelection.SEARCH_BAR }.text = RegionSelection.REGION
            onElement { viewIdResourceName == RegionSelection.SEARCH_LOCATION_TEXT }.click()
            assertConnected()
        }
}