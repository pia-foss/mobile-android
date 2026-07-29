package e2e

import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import androidx.test.uiautomator.uiAutomator
import junit.framework.TestCase.assertNotNull
import org.junit.Test

private const val AUTOMATION_SCREEN_TEXT = "Setup rules to handle auto-connect rules"
private const val KILL_SWITCH_SCREEN_TEXT = "Go to Network"

class QuickSettingsTests {
    @Test
    fun quickSettingsButtonsNavigateToTheirRespectiveScreens() =
        uiAutomator {
            login()

            onElement { viewIdResourceName == QuickSettings.AUTOMATION_BUTTON }.click()
            assertNotNull(
                device.wait(Until.hasObject(By.textStartsWith(AUTOMATION_SCREEN_TEXT)), TIMEOUT),
            )
            device.pressBack()
            device.waitForIdle()

            onElement { viewIdResourceName == QuickSettings.KILL_SWITCH_BUTTON }.click()
            assertNotNull(
                device.wait(Until.hasObject(By.textStartsWith(KILL_SWITCH_SCREEN_TEXT)), TIMEOUT),
            )
            device.pressBack()
            device.waitForIdle()

            onElement { viewIdResourceName == QuickSettings.PROTOCOLS_BUTTON }.click()
            onElement { viewIdResourceName == Protocols.PROTOCOL_SELECTION_BUTTON }
            device.pressBack()
            device.waitForIdle()
        }
}