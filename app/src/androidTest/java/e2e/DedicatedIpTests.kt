package e2e

import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import androidx.test.uiautomator.uiAutomator
import com.kape.vpn.BuildConfig
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import org.junit.Test

class DedicatedIpTests {
    @Test
    fun acceptValidToken() =
        uiAutomator {
            login()
            onElement { viewIdResourceName == Main.SIDE_MENU }.click()
            device.wait(Until.hasObject(By.res(SideMenu.USERNAME).textStartsWith("p")), TIMEOUT)
            onElement { viewIdResourceName == SideMenu.DEDICATED_IP }.click()
            onElement { viewIdResourceName == Dip.FIELD }.text = BuildConfig.PIA_VALID_DIP_TOKEN
            onElement { viewIdResourceName == Dip.ACTIVATE_BUTTON }.click()

            assertNotNull(
                device.wait(Until.hasObject(By.res(Dip.FLAG)), TIMEOUT),
            )
            assertNotNull(
                device.wait(Until.hasObject(By.res(Dip.SERVER_NAME)), TIMEOUT),
            )
            assertTrue(
                device.wait(Until.gone(By.res(Dip.FIELD)), TIMEOUT),
            )
        }

    @Test
    fun rejectInvalidDip() =
        uiAutomator {
            login()
            onElement { viewIdResourceName == Main.SIDE_MENU }.click()
            device.wait(Until.hasObject(By.res(SideMenu.USERNAME).textStartsWith("p")), TIMEOUT)
            onElement { viewIdResourceName == SideMenu.DEDICATED_IP }.click()
            device.wait(Until.hasObject(By.res(Dip.FIELD)), TIMEOUT)
            onElement { viewIdResourceName == Dip.FIELD }.text = INVALID
            onElement { viewIdResourceName == Dip.ACTIVATE_BUTTON }.click()

            assertNotNull(device.findObject(By.res(Dip.FIELD)))
        }
}