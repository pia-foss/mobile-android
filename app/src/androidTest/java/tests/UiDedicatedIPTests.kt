package tests

import androidx.test.uiautomator.uiAutomator
import com.kape.vpn.BuildConfig
import org.junit.Before
import org.junit.Test
import screens.helpers.UiAutomatorHelpers.LONG_TIMEOUT
import screens.helpers.UiAutomatorHelpers.findByResId
import screens.steps.DedicatedIPSteps
import screens.steps.DedicatedIPSteps.activateDedicatedIPToken
import screens.steps.DedicatedIPSteps.navigateToDedicatedIPPage
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class UiDedicatedIPTests : UiTest() {
    @Before
    fun setUp() {
        setupWithFreshLogin()
    }

    @Test
    fun accept_valid_dedicated_ip_token() = uiAutomator {
        // Navigate to Dedicated IP screen
        navigateToDedicatedIPPage()

        // Enter a valid token and activate
        activateDedicatedIPToken(BuildConfig.PIA_VALID_DIP_TOKEN)

        // Wait for server name and flag to appear (network round-trip required)
        assertNotNull(findByResId(DedicatedIPSteps.DEDICATED_IP_SERVER_NAME, LONG_TIMEOUT))
        assertNotNull(findByResId(DedicatedIPSteps.DEDICATED_IP_FLAG, LONG_TIMEOUT))

        // Verify that the input field is gone after successful activation
        assertNull(findByResId(DedicatedIPSteps.DEDICATED_IP_FIELD))
    }

    @Test
    fun reject_invalid_dedicated_ip_token() = uiAutomator {
        navigateToDedicatedIPPage()
        activateDedicatedIPToken("InvalidToken")

        // Input field should still be present
        assertNotNull(findByResId(DedicatedIPSteps.DEDICATED_IP_FIELD))
    }

    @Test
    fun reject_empty_dedicated_ip_token() = uiAutomator {
        navigateToDedicatedIPPage()
        activateDedicatedIPToken("")

        // Input field should still be present
        assertNotNull(findByResId(DedicatedIPSteps.DEDICATED_IP_FIELD))
    }
}
