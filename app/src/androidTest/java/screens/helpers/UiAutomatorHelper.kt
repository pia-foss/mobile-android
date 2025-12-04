package screens.helpers

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2

object UiAutomatorHelpers {

    val device: UiDevice =
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    const val DEFAULT_TIMEOUT = 5000L
    const val LONG_TIMEOUT = 10000L
    private const val POLL_INTERVAL = 200L

    /** Find an element by resource ID and wait until visible */
    fun findByResId(resId: String, timeout: Long = DEFAULT_TIMEOUT): UiObject2? {
        val startTime = System.currentTimeMillis()
        var element: UiObject2? = null
        while (System.currentTimeMillis() - startTime < timeout) {
            element = device.findObject(By.res(resId))
            if (element != null && isVisible(element)) return element
            Thread.sleep(POLL_INTERVAL)
        }
        return element
    }

    /** Find an element by text and wait until visible */
    fun findByText(text: String, timeout: Long = DEFAULT_TIMEOUT): UiObject2? {
        val startTime = System.currentTimeMillis()
        var element: UiObject2? = null
        while (System.currentTimeMillis() - startTime < timeout) {
            element = device.findObject(By.text(text))
            if (element != null && isVisible(element)) return element
            Thread.sleep(POLL_INTERVAL)
        }
        return element
    }

    /** Check if element is visible on screen */
    fun isVisible(element: UiObject2): Boolean {
        val bounds = element.visibleBounds
        return element.isEnabled && bounds.width() > 0 && bounds.height() > 0
    }

    /** Wait until element is visible */
    fun waitUntilVisible(element: UiObject2?, timeout: Long = DEFAULT_TIMEOUT): Boolean {
        if (element == null) return false
        val startTime = System.currentTimeMillis()
        while (System.currentTimeMillis() - startTime < timeout) {
            val bounds = element.visibleBounds
            if (element.isEnabled && bounds.width() > 0 && bounds.height() > 0) {
                return true
            }
            Thread.sleep(200) // small poll interval
        }
        return false
    }


    /** Click an element safely and wait for UI to settle */
    fun click(element: UiObject2?, timeout: Long = DEFAULT_TIMEOUT) {
        if (element != null) {
            element.click()
            device.waitForIdle(timeout)
        }
    }

    /** Click an element and wait for new window (alternative) */
    fun clickAndWaitForNewWindow(element: UiObject2?, timeout: Long = DEFAULT_TIMEOUT) {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        if (element != null) {
            element.click()
            val startTime = System.currentTimeMillis()
            while (System.currentTimeMillis() - startTime < timeout) {
                device.waitForIdle(POLL_INTERVAL)  // just waits, returns Unit
                // Optionally, you could check some condition to break early
            }
        }
    }


    /** Input text into a field safely */
    fun inputText(element: UiObject2?, text: String) {
        if (element != null) {
            element.click()
            element.clear()
            // Use adb shell command to input text
            // Escape spaces and special characters
            val escapedText = text.replace(" ", "%s")
            val command = "input text $escapedText"

            device.executeShellCommand(command)
        } else {
            throw IllegalArgumentException("UI element is null")
        }
    }

    /** Wait for VPN connection to establish by checking IP pattern */
    fun waitUntilConnectionIsEstablished(timeout: Long = DEFAULT_TIMEOUT): Boolean {
        val vpnIpRegex = "^((\\d{1,3})\\.){3}(\\d{1,3})$".toRegex()
        val startTime = System.currentTimeMillis()
        while (System.currentTimeMillis() - startTime < timeout) {
            val vpnIpField = findByResId(":Text:vpnIp", POLL_INTERVAL)
            if (vpnIpField != null && vpnIpRegex.matches(vpnIpField.text)) return true
            Thread.sleep(POLL_INTERVAL)
        }
        return false
    }
}
