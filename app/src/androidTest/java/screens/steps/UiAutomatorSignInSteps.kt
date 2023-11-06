package screens.steps

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject
import androidx.test.uiautomator.Until
import screens.objects.AppPermissionObjects
import screens.objects.LoginUiObjects
import screens.objects.SignUpUiObjects
import tests.UiAutomatorTest.Companion.defaultTimeOut

class UiAutomatorSignInSteps : SignInSteps {

    private val device: UiDevice =
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    override fun clickOnLoginButtonSignUpScreen() {
        SignUpUiObjects.loginButton.clickAndWaitForNewWindow(defaultTimeOut)
    }

    override fun enterCredentials(username: String, password: String) {
        inputTextInField(LoginUiObjects.usernameField, username)
        inputTextInField(LoginUiObjects.passwordField, password)
    }

    override fun clickOnLoginButtonLoginScreen() {
        LoginUiObjects.loginButton.clickAndWaitForNewWindow(defaultTimeOut)
    }

    override fun allowVpnProfileCreation() {
        clickIfExists(AppPermissionObjects.vpnProfileOkButton)
        AppPermissionObjects.androidOkButton.clickAndWaitForNewWindow(defaultTimeOut)
    }

    override fun allowNotifications() {
        clickIfExists(AppPermissionObjects.appAllowNotifications)
        AppPermissionObjects.androidAllowNotifications.clickAndWaitForNewWindow(defaultTimeOut)
    }

    private fun <T> inputTextInField(field: UiObject, data: T?) {
        field.click()
        field.legacySetText(data?.toString() ?: "")
    }

    private fun clickIfExists(primaryUiObject: UiObject, secondaryUiObj: UiObject? = null) {
        if (primaryUiObject.exists()) {
            device.wait((Until.findObject(By.res(primaryUiObject.text))), defaultTimeOut)
            (secondaryUiObj ?: primaryUiObject).clickAndWaitForNewWindow(defaultTimeOut)
        }
    }
}