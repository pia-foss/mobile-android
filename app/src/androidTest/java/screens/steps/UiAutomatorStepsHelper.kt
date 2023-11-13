package screens.steps

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject
import androidx.test.uiautomator.Until

object UiAutomatorStepsHelper {

    val device: UiDevice =
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    fun <T> inputTextInField(field: UiObject, data: T?) {
        field.click()
        field.legacySetText(data?.toString() ?: "")
    }

    fun waitUntilFound(uiObject: UiObject) {
        device.wait((Until.findObject(By.res(uiObject.text))), defaultTimeOut)
    }

    const val defaultTimeOut = 5000L
}