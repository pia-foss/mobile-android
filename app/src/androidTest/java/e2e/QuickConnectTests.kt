package e2e

import androidx.test.uiautomator.uiAutomator
import org.junit.Test

class QuickConnectTests {
    @Test
    fun quickConnectFromDisconnectedConnects() =
        uiAutomator {
            login()
            onElement { viewIdResourceName == Main.CONNECT_BUTTON }.click()
            assertConnected()
            onElement { viewIdResourceName == Main.CONNECT_BUTTON }.click()
            onElement { viewIdResourceName == Main.QUICK_CONNECT_FIRST_ITEM }.click()
            assertConnected()
        }

    @Test
    fun quickConnectSwitchesServerWhenAlreadyConnected() =
        uiAutomator {
            login()
            onElement { viewIdResourceName == Main.CONNECT_BUTTON }.click()
            assertConnected()
            onElement { viewIdResourceName == Main.LOCATION_PICKER }.click()
            onElement { viewIdResourceName == Main.LOCATION_EIGHT_ITEM }.click()
            onElement { viewIdResourceName == Main.CHANGE_LOCATION_CONFIRM }.click()
            assertConnected()
            onElement { viewIdResourceName == Main.QUICK_CONNECT_SECOND_ITEM }.click()
            assertConnected()
        }
}