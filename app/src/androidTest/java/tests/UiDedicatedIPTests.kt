package tests

import com.kape.vpn.BuildConfig
import org.junit.Test
import screens.helpers.UiAutomatorHelpers.waitUntilVisible
import kotlin.test.assertTrue

class UiDedicatedIPTests : UiTest() {

    @Test
    fun accept_valid_dedicated_ip_token() {
        // Navigate to Dedicated IP screen
        dedicatedIPSteps.navigateToDedicatedIPPage()

        // Enter a valid token and activate
        dedicatedIPSteps.activateDedicatedIPToken(BuildConfig.PIA_VALID_DIP_TOKEN)

        // Wait for server name and flag to appear
        assertTrue(waitUntilVisible(dedicatedIPSteps.dedicatedIPServerName))
        assertTrue(waitUntilVisible(dedicatedIPSteps.dedicatedIPFlag))

        // Verify that the input field is gone after successful activation
        assertTrue(dedicatedIPSteps.dedicatedIPField == null)
    }

    @Test
    fun reject_invalid_dedicated_ip_token() {
        dedicatedIPSteps.navigateToDedicatedIPPage()
        dedicatedIPSteps.activateDedicatedIPToken("InvalidToken")

        // Input field should still be present
        assertTrue(waitUntilVisible(dedicatedIPSteps.dedicatedIPField))
    }

    @Test
    fun reject_empty_dedicated_ip_token() {
        dedicatedIPSteps.navigateToDedicatedIPPage()
        dedicatedIPSteps.activateDedicatedIPToken("")

        // Input field should still be present
        assertTrue(waitUntilVisible(dedicatedIPSteps.dedicatedIPField))
    }
}
