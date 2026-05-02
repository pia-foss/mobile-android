package e2e

import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import androidx.test.uiautomator.uiAutomator
import org.junit.Test

class ProtocolTests {
    @Test
    fun protocolsConnectWithAndWithoutSmallPacketsEnabled() =
        uiAutomator {
            login()
            // OpenVpn
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
            onElement { viewIdResourceName == Main.CONNECT_BUTTON }.click()
            assertConnected()

            // disconnect
            onElement { viewIdResourceName == Main.CONNECT_BUTTON }.click()
            onElement { viewIdResourceName == Main.SIDE_MENU }.click()
            device.wait(Until.hasObject(By.res(SideMenu.USERNAME).textStartsWith("p")), TIMEOUT)
            onElement { viewIdResourceName == SideMenu.SETTINGS_BUTTON }.click()
            onElement { viewIdResourceName == Settings.PROTOCOLS_BUTTON }.click()
            onElement { viewIdResourceName == Protocols.PROTOCOL_SELECTION_BUTTON }.click()
            onElement { viewIdResourceName == Protocols.OPEN_VPN_BUTTON }.click()
            onElement { viewIdResourceName == Protocols.ANDROID_OK_BUTTON }.click()
            onElement { viewIdResourceName == Protocols.SMALL_PACKETS_TOGGLE }.click()
            device.pressBack()
            device.waitForIdle()
            device.pressBack()
            device.waitForIdle()
            onElement { viewIdResourceName == Main.CONNECT_BUTTON }.click()
            assertConnected()

            // Wireguard
            // disconnect
            onElement { viewIdResourceName == Main.CONNECT_BUTTON }.click()
            onElement { viewIdResourceName == Main.SIDE_MENU }.click()
            device.wait(Until.hasObject(By.res(SideMenu.USERNAME).textStartsWith("p")), TIMEOUT)
            onElement { viewIdResourceName == SideMenu.SETTINGS_BUTTON }.click()
            onElement { viewIdResourceName == Settings.PROTOCOLS_BUTTON }.click()
            onElement { viewIdResourceName == Protocols.PROTOCOL_SELECTION_BUTTON }.click()
            onElement { viewIdResourceName == Protocols.WIRE_GUARD_BUTTON }.click()
            onElement { viewIdResourceName == Protocols.ANDROID_OK_BUTTON }.click()
            device.pressBack()
            device.waitForIdle()
            device.pressBack()
            device.waitForIdle()
            onElement { viewIdResourceName == Main.CONNECT_BUTTON }.click()
            assertConnected()

            // disconnect
            onElement { viewIdResourceName == Main.CONNECT_BUTTON }.click()
            onElement { viewIdResourceName == Main.SIDE_MENU }.click()
            device.wait(Until.hasObject(By.res(SideMenu.USERNAME).textStartsWith("P")), TIMEOUT)
            onElement { viewIdResourceName == SideMenu.SETTINGS_BUTTON }.click()
            onElement { viewIdResourceName == Settings.PROTOCOLS_BUTTON }.click()
            onElement { viewIdResourceName == Protocols.PROTOCOL_SELECTION_BUTTON }.click()
            onElement { viewIdResourceName == Protocols.WIRE_GUARD_BUTTON }.click()
            onElement { viewIdResourceName == Protocols.ANDROID_OK_BUTTON }.click()
            onElement { viewIdResourceName == Protocols.SMALL_PACKETS_TOGGLE }.click()
            device.pressBack()
            device.waitForIdle()
            device.pressBack()
            device.waitForIdle()
            onElement { viewIdResourceName == Main.CONNECT_BUTTON }.click()
            assertConnected()
        }
}