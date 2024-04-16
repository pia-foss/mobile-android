package screens.steps

import screens.helpers.UiAutomatorObjectFinder
import screens.helpers.UiAutomatorStepsHelper
import screens.helpers.UiAutomatorStepsHelper.defaultTimeout

object LoginSteps {
    val usernameField = UiAutomatorObjectFinder.findByResourceId(":LoginScreen:enter_username")
    val passwordField = UiAutomatorObjectFinder.findByResourceId(":LoginScreen:enter_password")
    val loginButton = UiAutomatorObjectFinder.findByResourceId(":LoginScreen:login_button")
    val vpnProfileOkButton = UiAutomatorObjectFinder.findByResourceId(":VpnPermissionScreen:ok")
    val androidOkButton = UiAutomatorObjectFinder.findByResourceId("android:id/button1")
    val appAllowNotifications =
        UiAutomatorObjectFinder.findByResourceId(":NotificationPermissionScreen:notifications_action")
    val androidAllowNotifications =
        UiAutomatorObjectFinder.findByResourceId("com.android.permissioncontroller:id/permission_allow_button")


    fun navigateToLoginScreen(){
        SignUpSteps.loginButton.click()
        UiAutomatorStepsHelper.waitUntilFound(usernameField)
    }
    fun logIn(username: String, password: String) {
        UiAutomatorStepsHelper.inputTextInField(usernameField, username)
        UiAutomatorStepsHelper.inputTextInField(passwordField, password)
        loginButton.clickAndWaitForNewWindow()
    }

    fun allowVpnProfileCreation() {
        // The first time we successfully sign in we will be prompted to allow permissions to create
        // the VPN profile. Once accepted, all the next tests will not be prompted to do this step
        if (vpnProfileOkButton.exists()) {
            UiAutomatorStepsHelper.waitUntilFound(vpnProfileOkButton)
            vpnProfileOkButton.clickAndWaitForNewWindow()
            androidOkButton.clickAndWaitForNewWindow(defaultTimeout)
        }
    }

    fun allowNotifications() {
        // The first time we successfully sign in we will be prompted to allow app notifications
        // Once accepted, all the next tests will not be prompted to do this step
        if (appAllowNotifications.exists()) {
            UiAutomatorStepsHelper.waitUntilFound(appAllowNotifications)
            appAllowNotifications.clickAndWaitForNewWindow(defaultTimeout
            )
            androidAllowNotifications.clickAndWaitForNewWindow(defaultTimeout
            )
        }
    }

    fun giveAppPermissions() {
        allowVpnProfileCreation()
        allowNotifications()
    }
}
