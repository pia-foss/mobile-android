package e2e

import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import androidx.test.uiautomator.uiAutomator
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import org.junit.Test

class HelpSettingsTests {
    @Test
    fun helpScreenOptionsAreInteractable() =
        uiAutomator {
            login()
            navigateToHelpScreen()

            assertTrue(
                onElement { viewIdResourceName == Help.VERSION }.isEnabled,
            )

            assertInteractableAndClick(Help.VIEW_DEBUG_LOG)
            assertNotNull(
                device.wait(Until.hasObject(By.textContains("VPN Debug Logs")), TIMEOUT),
            )
        }

    @Test
    fun viewSharedDataIsAccessible() =
        uiAutomator {
            login()
            navigateToHelpScreen()

            assertInteractableAndClick(Help.IMPROVE_PIA_TOGGLE)
            assertInteractableAndClick(Help.VIEW_SHARED_DATA)
            assertNotNull(
                device.wait(Until.hasObject(By.textContains("Connection Stats")), TIMEOUT),
            )
        }
}