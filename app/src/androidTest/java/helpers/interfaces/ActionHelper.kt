package helpers.interfaces

import androidx.test.uiautomator.UiObject

interface ActionHelpersInterface {
    fun clickIfExists(primaryUiObject: UiObject, secondaryUiObj: UiObject? = null)
    fun <T> inputTextInField(field: UiObject, data: T? = null)
    fun trySignIn(username: String, password: String)
    fun giveAppPermissions()
}