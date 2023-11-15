package screens.steps

import screens.objects.AppPermissionObjects
import screens.objects.LoginUiObjects
import screens.objects.SignUpUiObjects
import screens.steps.UiAutomatorStepsHelper.defaultTimeOut
import screens.steps.UiAutomatorStepsHelper.device
import screens.steps.UiAutomatorStepsHelper.inputTextInField
import screens.steps.UiAutomatorStepsHelper.waitUntilFound


class UiAutomatorSignInSteps : SignInSteps {

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
        // The first time we successfully sign in we will be prompted to allow permissions to create
        // the VPN profile. Once accepted, all the next tests will not be prompted to do this step
        if (AppPermissionObjects.vpnProfileOkButton.exists()) {
            waitUntilFound(AppPermissionObjects.vpnProfileOkButton)
            AppPermissionObjects.vpnProfileOkButton.clickAndWaitForNewWindow()
            AppPermissionObjects.androidOkButton.clickAndWaitForNewWindow(defaultTimeOut)
        }
    }

    override fun allowNotifications() {
        // The first time we successfully sign in we will be prompted to allow app notifications
        // Once accepted, all the next tests will not be prompted to do this step
        if (AppPermissionObjects.appAllowNotifications.exists()) {
            waitUntilFound(AppPermissionObjects.appAllowNotifications)
            AppPermissionObjects.appAllowNotifications.clickAndWaitForNewWindow(defaultTimeOut)
            AppPermissionObjects.androidAllowNotifications.clickAndWaitForNewWindow(defaultTimeOut)
        }
    }

    override fun navigateToSignUpScreen() {
        if (LoginUiObjects.loginButton.exists())
            device.pressBack()
        // TODO: Add logic to navigate from other screens

        waitUntilFound(SignUpUiObjects.loginButton)
        assert(SignUpUiObjects.loginButton.exists())
    }
}