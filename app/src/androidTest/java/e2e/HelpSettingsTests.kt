package e2e

import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import androidx.test.uiautomator.uiAutomator
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import org.junit.Test

// Split into separate @Test methods (rather than one walk through every Help row) so Test
// Orchestrator runs each in its own process - viewing the debug log, a real Send Log network
// round trip, and the Shared Data screen all in a single test were accumulating enough memory
// in one process to crash the CI emulator.
class HelpSettingsTests {
    @Test
    fun helpScreenOptionsAreInteractable() =
        uiAutomator {
            login()
            navigateToHelpScreen()

            // The Version row opens the Play Store via a market:// intent, which isn't
            // guaranteed to resolve on a CI emulator - confirm it's displayed and enabled
            // without following it out of the app.
            assertTrue(
                onElement { viewIdResourceName == Help.VERSION }.isEnabled,
            )

            assertInteractableAndClick(Help.VIEW_DEBUG_LOG)
            assertNotNull(
                device.wait(Until.hasObject(By.textContains("VPN Debug Logs")), TIMEOUT),
            )
        }

    @Test
    fun sendLogSucceeds() =
        uiAutomator {
            login()
            navigateToHelpScreen()

            assertInteractableAndClick(Help.ENABLE_DEBUG_LOGGING_TOGGLE)

            // A real Send Log network round trip - assert the success dialog and dismiss it.
            assertInteractableAndClick(Help.SEND_LOG)
            assertNotNull(
                device.wait(Until.hasObject(By.res(Help.SEND_LOG_SUCCESS_OK)), NETWORK_TRANSITION_TIMEOUT),
            )
            onElement { viewIdResourceName == Help.SEND_LOG_SUCCESS_OK }.click()
        }

    @Test
    fun viewSharedDataIsAccessible() =
        uiAutomator {
            login()
            navigateToHelpScreen()

            // Improve PIA defaults to off; enabling it reveals the View Shared Data row.
            assertInteractableAndClick(Help.IMPROVE_PIA_TOGGLE)
            assertInteractableAndClick(Help.VIEW_SHARED_DATA)
            assertNotNull(
                device.wait(Until.hasObject(By.textContains("Connection Stats")), TIMEOUT),
            )
        }
}