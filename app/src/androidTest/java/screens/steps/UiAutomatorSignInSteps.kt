package screens.steps

import screens.objects.AppPermissionObjects
import screens.objects.LoginUiObjects
import screens.objects.SignUpUiObjects
import screens.steps.helpers.UiAutomatorStepsHelper.defaultTimeout
import screens.steps.helpers.UiAutomatorStepsHelper.device
import screens.steps.helpers.UiAutomatorStepsHelper.inputTextInField
import screens.steps.helpers.UiAutomatorStepsHelper.waitUntilFound
import screens.steps.interfaces.SignInSteps


class UiAutomatorSignInSteps : SignInSteps {

    override fun clickOnLoginButtonSignUpScreen() {
        SignUpUiObjects.loginButton.clickAndWaitForNewWindow(defaultTimeout)
    }

    override fun enterCredentials(username: String, password: String) {
        inputTextInField(LoginUiObjects.usernameField, username)
        inputTextInField(LoginUiObjects.passwordField, password)
    }

    override fun clickOnLoginButtonLoginScreen() {
        LoginUiObjects.loginButton.clickAndWaitForNewWindow(defaultTimeout)
    }

    override fun allowVpnProfileCreation() {
        // The first time we successfully sign in we will be prompted to allow permissions to create
        // the VPN profile. Once accepted, all the next tests will not be prompted to do this step
        if (AppPermissionObjects.vpnProfileOkButton.exists()) {
            waitUntilFound(AppPermissionObjects.vpnProfileOkButton)
            AppPermissionObjects.vpnProfileOkButton.clickAndWaitForNewWindow()
            AppPermissionObjects.androidOkButton.clickAndWaitForNewWindow(defaultTimeout)
        }
    }

    override fun allowNotifications() {
        // The first time we successfully sign in we will be prompted to allow app notifications
        // Once accepted, all the next tests will not be prompted to do this step
        if (AppPermissionObjects.appAllowNotifications.exists()) {
            waitUntilFound(AppPermissionObjects.appAllowNotifications)
            AppPermissionObjects.appAllowNotifications.clickAndWaitForNewWindow(defaultTimeout)
            AppPermissionObjects.androidAllowNotifications.clickAndWaitForNewWindow(defaultTimeout)
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