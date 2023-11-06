package com.privateinternetaccess.android.screens.steps

import com.privateinternetaccess.android.helpers.ActionHelpers.clickIfExists
import com.privateinternetaccess.android.helpers.ActionHelpers.inputTextInField
import com.privateinternetaccess.android.screens.objects.SignIn
import com.privateinternetaccess.android.core.BaseUiAutomatorClass.Companion.defaultTimeOut

class SignInStepObjects {

    fun reachSignInScreen() {
        SignIn.reachLoginScreenButton.clickAndWaitForNewWindow(defaultTimeOut)
    }

    fun enterCredentials(username : String,  password : String) {
        inputTextInField(SignIn.usernameField, username)
        inputTextInField(SignIn.passwordField, password)
    }

    fun clickOnLoginButton() {
        SignIn.loginButton.clickAndWaitForNewWindow(defaultTimeOut)
    }

    fun allowVpnProfileCreation() {
        clickIfExists(SignIn.vpnProfileOkButton)
        SignIn.androidOkButton.clickAndWaitForNewWindow(defaultTimeOut)
    }

    fun allowNotifications() {
        clickIfExists(SignIn.appAllowNotifications)
        SignIn.androidAllowNotifications.clickAndWaitForNewWindow(defaultTimeOut)
    }
}