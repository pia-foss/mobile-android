package screens.helpers

import androidx.test.uiautomator.By
import androidx.test.uiautomator.ElementNotFoundException
import androidx.test.uiautomator.UiAutomatorTestScope
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import androidx.test.uiautomator.textAsString

object UiAutomatorHelpers {

    const val DEFAULT_TIMEOUT = 5000L
    const val LONG_TIMEOUT = 30000L
    const val VPN_CONNECT_TIMEOUT = 60000L

    fun UiAutomatorTestScope.findByResId(
        resId: String,
        timeout: Long = DEFAULT_TIMEOUT,
    ): UiObject2? {
        return try {
            onElement(timeout) { viewIdResourceName == resId }
        } catch (e: ElementNotFoundException) {
            null
        }
    }

    fun UiAutomatorTestScope.findByPartialText(
        text: String,
        timeout: Long = DEFAULT_TIMEOUT,
    ): UiObject2? {
        return try {
            onElement(timeout) { textAsString()?.contains(text) == true }
        } catch (e: ElementNotFoundException) {
            null
        }
    }

    fun UiAutomatorTestScope.findByStartsWith(
        text: String,
        timeout: Long = DEFAULT_TIMEOUT,
    ): UiObject2? {
        return try {
            onElement(timeout) { textAsString()?.startsWith(text) == true }
        } catch (e: ElementNotFoundException) {
            null
        }
    }

    fun UiAutomatorTestScope.click(
        resId: String,
        timeout: Long = DEFAULT_TIMEOUT,
    ) {
        onElement(timeout) { viewIdResourceName == resId }.click()
        // Let animations/navigation settle
        device.waitForIdle()
    }

    fun UiAutomatorTestScope.pressBackTwice(times: Int = 1) {
        repeat(times) {
            findFreshByResId(":AppBar:back").click()
            device.waitForIdle()
        }
    }

    private fun UiAutomatorTestScope.findFreshByResId(resId: String): UiObject2 {
        return device.wait(Until.findObject(By.res(resId)), DEFAULT_TIMEOUT)
            ?: error("Element with resId '$resId' not found within timeout")
    }

    fun UiAutomatorTestScope.inputText(
        resId: String,
        text: String,
    ) {
        findByResId(resId)?.clear()
        findByResId(resId)?.click()

        // Native UiAutomator typing — ordered and synced
        val escapedText = text.replace(" ", "%s")
        val command = "input text $escapedText"

        device.executeShellCommand(command)

        device.waitForIdle()
    }

    fun UiAutomatorTestScope.waitUntilConnectionIsEstablished(
        timeout: Long = VPN_CONNECT_TIMEOUT,
    ): Boolean {

        val vpnIpRegex = "^((\\d{1,3})\\.){3}(\\d{1,3})$".toRegex()
        val endTime = System.currentTimeMillis() + timeout

        // Fast-fail if the IP field never appears on screen
        device.wait(Until.hasObject(By.res(":Text:vpnIp")), timeout.coerceAtMost(2_000))
            ?: return false

        // Re-query on every iteration to avoid stale references during recomposition
        while (System.currentTimeMillis() < endTime) {
            val text = device.findObject(By.res(":Text:vpnIp"))?.text
            if (text != null && vpnIpRegex.matches(text)) {
                return true
            }
            device.waitForIdle(150)
        }

        return false
    }
}
