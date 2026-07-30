package e2e

import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import androidx.test.uiautomator.uiAutomator
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import org.junit.Test

class SideMenuTests {
    @Test
    fun accountIsInteractable() =
        uiAutomator {
            login()
            openSideMenu()
            assertInteractableAndClick(SideMenu.ACCOUNT)
            assertNotNull(
                device.wait(Until.hasObject(By.textContains("Username")), TIMEOUT),
            )
        }

    @Test
    fun dedicatedIpIsInteractable() =
        uiAutomator {
            login()
            openSideMenu()
            assertInteractableAndClick(SideMenu.DEDICATED_IP)
            assertNotNull(
                device.wait(Until.hasObject(By.res(Dip.FIELD)), TIMEOUT),
            )
        }

    @Test
    fun perAppSettingsIsInteractable() =
        uiAutomator {
            login()
            openSideMenu()
            assertInteractableAndClick(SideMenu.PER_APP_SETTINGS)
            assertNotNull(
                device.wait(Until.hasObject(By.textContains("Per App Settings")), TIMEOUT),
            )
        }

    @Test
    fun settingsIsInteractable() =
        uiAutomator {
            login()
            openSideMenu()
            assertInteractableAndClick(SideMenu.SETTINGS_BUTTON)
            assertNotNull(
                device.wait(Until.hasObject(By.res(Settings.GENERAL_BUTTON)), TIMEOUT),
            )
        }

    @Test
    fun aboutIsInteractable() =
        uiAutomator {
            login()
            openSideMenu()
            assertInteractableAndClick(SideMenu.ABOUT)
            assertNotNull(
                device.wait(Until.hasObject(By.textContains("About")), TIMEOUT),
            )
        }

    @Test
    fun privacyPolicyIsInteractable() =
        uiAutomator {
            login()
            openSideMenu()
            assertTrue(
                onElement { viewIdResourceName == SideMenu.PRIVACY_POLICY }.isEnabled,
            )
        }

    @Test
    fun contactSupportIsInteractable() =
        uiAutomator {
            login()
            openSideMenu()
            assertTrue(
                onElement { viewIdResourceName == SideMenu.CONTACT_SUPPORT }.isEnabled,
            )
        }

    @Test
    fun logoutIsInteractable() =
        uiAutomator {
            login()
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