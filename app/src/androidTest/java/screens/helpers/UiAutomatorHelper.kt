package screens.helpers

import androidx.test.uiautomator.By
import androidx.test.uiautomator.ElementNotFoundException
import androidx.test.uiautomator.UiAutomatorTestScope
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import androidx.test.uiautomator.textAsString

object UiAutomatorHelpers {

    const val DEFAULT_TIMEOUT = 5000L
    const val LONG_TIMEOUT = 10000L

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
        }
    }

    private fun UiAutomatorTestScope.findFreshByResId(resId: String): UiObject2 {
        device.wait(Until.hasObject(By.res(resId)), DEFAULT_TIMEOUT)
        return device.findObject(By.res(resId))
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
        timeout: Long = LONG_TIMEOUT,
    ): Boolean {

        val vpnIpRegex = "^((\\d{1,3})\\.){3}(\\d{1,3})$".toRegex()
        val endTime = System.currentTimeMillis() + timeout

        // Wait until the IP field exists first (fast-fail if screen wrong)
        val ipField = device.wait(
            Until.findObject(By.res(":Text:vpnIp")),
            timeout.coerceAtMost(2_000),
        ) ?: return false

        // Now poll ONLY the text (cheap + stable)
        while (System.currentTimeMillis() < endTime) {

            val text = ipField.text

            if (text != null && vpnIpRegex.matches(text)) {
                return true
            }

            device.waitForIdle(150)
        }

        return false
    }
}
