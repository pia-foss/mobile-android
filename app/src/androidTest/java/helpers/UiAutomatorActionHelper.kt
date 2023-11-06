package com.privateinternetaccess.android.helpers

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject
import androidx.test.uiautomator.Until
import com.kape.vpn.BuildConfig
import com.privateinternetaccess.android.core.BaseUiAutomatorClass.Companion.defaultTimeOut
import com.privateinternetaccess.android.screens.steps.SignInStepObjects

object ActionHelpers {

    private val device: UiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    fun clickIfExists(primaryUiObject : UiObject, secondaryUiObj: UiObject? = null) {
        if (primaryUiObject.exists()) {
            device.wait((Until.findObject(By.res(primaryUiObject.text))), defaultTimeOut)
            (secondaryUiObj ?: primaryUiObject).clickAndWaitForNewWindow(defaultTimeOut)
        }
    }

    fun <T> inputTextInField(field: UiObject, data: T? = null) {
        field.click()
        field.legacySetText(data?.toString() ?: "")
    }


    fun trySignIn(
        username : String = BuildConfig.PIA_VALID_USERNAME,
        password : String = BuildConfig.PIA_VALID_PASSWORD
    ) {
        SignInStepObjects().reachSignInScreen()
        SignInStepObjects().enterCredentials(username, password)
        SignInStepObjects().clickOnLoginButton()
    }
    fun giveAppPermissions()
    {
        SignInStepObjects().allowVpnProfileCreation()
        SignInStepObjects().allowNotifications()
    }

}