package e2e

import android.webkit.WebView
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import androidx.test.uiautomator.uiAutomator
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import org.junit.Test

// Returning to the connection screen re-triggers its LaunchedEffect(Unit) autoConnect check,
// so the hamburger button can take a beat longer to settle than a plain connect-button wait -
// give it more headroom than the default onElement timeout before giving up.
private fun openSideMenu() =
    uiAutomator {
        device.waitForIdle()
        onElement(timeoutMs = NETWORK_TRANSITION_TIMEOUT) { viewIdResourceName == Main.SIDE_MENU }.click()
        device.wait(Until.hasObject(By.res(SideMenu.USERNAME).textStartsWith("p")), TIMEOUT)
    }

private fun assertInteractableAndClick(viewId: String) =
    uiAutomator {
        val item = onElement { viewIdResourceName == viewId }
        assertTrue(item.isEnabled)
        item.click()
    }

class SideMenuTests {
    @Test
    fun allSideMenuOptionsAreInteractable() =
        uiAutomator {
            login()

            openSideMenu()
            assertInteractableAndClick(SideMenu.ACCOUNT)
            assertNotNull(
                device.wait(Until.hasObject(By.textContains("Username")), TIMEOUT),
            )
            returnToConnectionScreen()

            openSideMenu()
            assertInteractableAndClick(SideMenu.DEDICATED_IP)
            assertNotNull(
                device.wait(Until.hasObject(By.res(Dip.FIELD)), TIMEOUT),
            )
            returnToConnectionScreen()

            openSideMenu()
            assertInteractableAndClick(SideMenu.PER_APP_SETTINGS)
            assertNotNull(
                device.wait(Until.hasObject(By.textContains("Per App Settings")), TIMEOUT),
            )
            returnToConnectionScreen()

            openSideMenu()
            assertInteractableAndClick(SideMenu.SETTINGS_BUTTON)
            assertNotNull(
                device.wait(Until.hasObject(By.res(Settings.GENERAL_BUTTON)), TIMEOUT),
            )
            returnToConnectionScreen()

            openSideMenu()
            assertInteractableAndClick(SideMenu.ABOUT)
            assertNotNull(
                device.wait(Until.hasObject(By.textContains("About")), TIMEOUT),
            )
            returnToConnectionScreen()

            openSideMenu()
            assertInteractableAndClick(SideMenu.PRIVACY_POLICY)
            assertNotNull(
                device.wait(Until.hasObject(By.clazz(WebView::class.java)), LONG_TIMEOUT),
            )
            returnToConnectionScreen()

            openSideMenu()
            assertInteractableAndClick(SideMenu.CONTACT_SUPPORT)
            assertNotNull(
                device.wait(Until.hasObject(By.clazz(WebView::class.java)), LONG_TIMEOUT),
            )
            returnToConnectionScreen()

            openSideMenu()
            assertInteractableAndClick(SideMenu.LOGOUT_BUTTON)
            assertNotNull(
                device.wait(Until.hasObject(By.res(SideMenu.LOGOUT_DIALOG_CONFIRM_BUTTON)), TIMEOUT),
            )
            onElement { viewIdResourceName == SideMenu.LOGOUT_DIALOG_DISMISS_BUTTON }.click()
            assertNotNull(
                device.wait(Until.hasObject(By.res(Main.CONNECT_BUTTON)), TIMEOUT),
            )
        }
}