package screens.helpers

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject
import androidx.test.uiautomator.UiSelector

object UiAutomatorObjectFinder {
    private val device: UiDevice =
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    fun findByResourceId(id: String, instance: Int = 0): UiObject {
        return device.findObject(UiSelector().resourceId(id).instance(instance))
    }

    fun findByText(text: String, instance: Int = 0): UiObject {
        return device.findObject(UiSelector().textMatches(text).instance(instance))
    }
}