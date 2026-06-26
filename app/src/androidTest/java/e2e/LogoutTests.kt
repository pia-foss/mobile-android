package e2e

import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import androidx.test.uiautomator.uiAutomator
import junit.framework.TestCase.assertNotNull
import org.junit.Test

class LogoutTests {
    @PhoneTest
    @Test
    fun logoutReachesSignupScreen() =
        uiAutomator {
            login()
            onElement { viewIdResourceName == Main.SIDE_MENU }.click()
            device.wait(Until.hasObject(By.res(SideMenu.USERNAME).textStartsWith("p")), TIMEOUT)
            onElement { viewIdResourceName == SideMenu.LOGOUT_BUTTON }.click()
            onElement { viewIdResourceName == SideMenu.LOGOUT_DIALOG_CONFIRM_BUTTON }.click()
            assertNotNull(
                device.wait(Until.hasObject(By.res(SignUp.LOGIN_BUTTON)), TIMEOUT),
            )
        }

    @PhoneTest
    @Test
    fun persistenceLayerWipedAfterLogout() =
        uiAutomator {
            login()
            onElement { viewIdResourceName == Main.SIDE_MENU }.click()
            device.wait(Until.hasObject(By.res(SideMenu.USERNAME).textStartsWith("p")), TIMEOUT)
            onElement { viewIdResourceName == SideMenu.SETTINGS_BUTTON }.click()
            onElement { viewIdResourceName == Settings.PROTOCOLS_BUTTON }.click()
            onElement { viewIdResourceName == Protocols.PROTOCOL_SELECTION_BUTTON }.click()
            onElement { viewIdResourceName == Protocols.OPEN_VPN_BUTTON }.click()
            onElement { viewIdResourceName == Protocols.ANDROID_OK_BUTTON }.click()
            device.pressBack()
            device.waitForIdle()
            device.pressBack()
            device.waitForIdle()
            onElement { viewIdResourceName == Main.SIDE_MENU }.click()
            device.wait(Until.hasObject(By.res(SideMenu.USERNAME).textStartsWith("p")), TIMEOUT)
            onElement { viewIdResourceName == SideMenu.LOGOUT_BUTTON }.click()
            device.waitForIdle()
            onElement { viewIdResourceName == SideMenu.LOGOUT_DIALOG_CONFIRM_BUTTON }.click()

            assertNotNull(
                device.wait(Until.hasObject(By.res(SignUp.LOGIN_BUTTON)), TIMEOUT),
            )
            loginFromCurrent()
            onElement { viewIdResourceName == Main.SIDE_MENU }.click()
            device.wait(Until.hasObject(By.res(SideMenu.USERNAME).textStartsWith("p")), TIMEOUT)
            onElement { viewIdResourceName == SideMenu.SETTINGS_BUTTON }.click()

            assertNotNull(
                device.wait(Until.hasObject(By.textContains("WireGuard")), TIMEOUT),
            )
        }
}