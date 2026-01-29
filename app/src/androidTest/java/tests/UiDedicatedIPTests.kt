package tests

import androidx.test.uiautomator.uiAutomator
import com.kape.vpn.BuildConfig
import org.junit.Test
import screens.helpers.UiAutomatorHelpers.elementExists
import screens.helpers.UiAutomatorHelpers.launchAppAndLogIn
import screens.steps.DedicatedIPSteps
import screens.steps.DedicatedIPSteps.activateDedicatedIPToken
import screens.steps.SideMenuSteps.navigateToDedicatedIp
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UiDedicatedIPTests {

    @Test
    fun accept_valid_dedicated_ip_token() = uiAutomator {
        launchAppAndLogIn()
        // Navigate to Dedicated IP screen
        navigateToDedicatedIp()

        // Enter a valid token and activate
        activateDedicatedIPToken(BuildConfig.PIA_VALID_DIP_TOKEN)

        // Wait for server name and flag to appear
        assertTrue { elementExists(DedicatedIPSteps.DEDICATED_IP_SERVER_NAME) }
        assertTrue { elementExists(DedicatedIPSteps.DEDICATED_IP_FLAG) }

        // Verify that the input field is gone after successful activation
        assertFalse { elementExists(DedicatedIPSteps.DEDICATED_IP_FIELD) }
    }

    @Test
    fun reject_invalid_dedicated_ip_token() = uiAutomator {
        launchAppAndLogIn()
        navigateToDedicatedIp()
        activateDedicatedIPToken("InvalidToken")

        // Input field should still be present
        assertTrue { elementExists(DedicatedIPSteps.DEDICATED_IP_FIELD) }
    }

    @Test
    fun reject_empty_dedicated_ip_token() = uiAutomator {
        launchAppAndLogIn()
        navigateToDedicatedIp()
        activateDedicatedIPToken("")

        // Input field should still be present
        assertTrue { elementExists(DedicatedIPSteps.DEDICATED_IP_FIELD) }
    }
}
