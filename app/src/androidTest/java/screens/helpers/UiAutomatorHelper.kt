package screens.helpers

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until

object UiAutomatorHelpers {

    val device: UiDevice =
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    const val DEFAULT_TIMEOUT = 5000L
    const val LONG_TIMEOUT = 10000L
    private const val POLL_INTERVAL = 200L

    /** Find an element by resource ID and wait until present & usable */
    fun findByResId(
        resId: String,
        timeout: Long = DEFAULT_TIMEOUT
    ): UiObject2? {
        val element = device.wait(
            Until.findObject(By.res(resId)),
            timeout
        )

        if (element != null) {
            val end = System.currentTimeMillis() + 1_000
            while (System.currentTimeMillis() < end) {
                if (element.isEnabled) return element
                device.waitForIdle(50)
            }
        }
        return element
    }


    /** Find an element by text and wait until present & usable */
    fun findByPartialText(
        text: String,
        timeout: Long = DEFAULT_TIMEOUT
    ): UiObject2? {

        // Let UiAutomator synchronize with layout & animations
        val element = device.wait(
            Until.findObject(By.textContains(text)),
            timeout
        )

        // Small enabled wait — Compose sometimes attaches first, enables a moment later
        if (element != null) {
            val end = System.currentTimeMillis() + 1_000
            while (System.currentTimeMillis() < end) {
                if (element.isEnabled) return element
                device.waitForIdle(50)
            }
        }

        return element
    }

    fun findByStartsWith(
        text: String,
        timeout: Long = DEFAULT_TIMEOUT
    ): UiObject2? {

        // Let UiAutomator synchronize with layout & animations
        val element = device.wait(
            Until.findObject(By.textStartsWith(text)),
            timeout
        )

        // Small enabled wait — Compose sometimes attaches first, enables a moment later
        if (element != null) {
            val end = System.currentTimeMillis() + 1_000
            while (System.currentTimeMillis() < end) {
                if (element.isEnabled) return element
                device.waitForIdle(50)
            }
        }

        return element
    }

    fun clickWhenReady(
        resId: String,
        timeout: Long = DEFAULT_TIMEOUT
    ) {
        val selector = By.res(resId)

        // Wait until element exists in the UI tree
        val element = device.wait(
            Until.findObject(selector),
            timeout
        ) ?: throw AssertionError("Element not found: $resId")

        // Ensure it is enabled (Compose sometimes enables slightly later)
        val end = System.currentTimeMillis() + 1_000
        while (System.currentTimeMillis() < end) {
            if (element.isEnabled) break
            device.waitForIdle(50)
        }

        element.click()

        // Let animations/navigation settle
        device.waitForIdle()
    }

    fun pressBackTwice(times: Int = 1) {
        repeat(times) {
            findFreshByResId(":AppBar:back").click()
        }
    }

    private fun findFreshByResId(resId: String): UiObject2 {
        device.wait(Until.hasObject(By.res(resId)), DEFAULT_TIMEOUT)
        return device.findObject(By.res(resId))
    }


    fun inputTextWhenReady(
        resId: String,
        text: String,
        timeout: Long = DEFAULT_TIMEOUT
    ) {
        val selector = By.res(resId)

        val field = device.wait(
            Until.findObject(selector),
            timeout
        ) ?: throw AssertionError("Input field not found: $resId")

        // Ensure field is interactable
        val end = System.currentTimeMillis() + 1_000
        while (System.currentTimeMillis() < end) {
            if (field.isEnabled) break
            device.waitForIdle(50)
        }

        field.click()
        field.clear()

        // Native UiAutomator typing — ordered and synced
        val escapedText = text.replace(" ", "%s")
        val command = "input text $escapedText"

        device.executeShellCommand(command)

        device.waitForIdle()
    }

    fun waitUntilConnectionIsEstablished(
        timeout: Long = LONG_TIMEOUT
    ): Boolean {

        val vpnIpRegex = "^((\\d{1,3})\\.){3}(\\d{1,3})$".toRegex()
        val endTime = System.currentTimeMillis() + timeout

        // Wait until the IP field exists first (fast-fail if screen wrong)
        val ipField = device.wait(
            Until.findObject(By.res(":Text:vpnIp")),
            timeout.coerceAtMost(2_000)
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
