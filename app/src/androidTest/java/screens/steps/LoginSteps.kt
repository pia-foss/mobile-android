package screens.steps

import screens.helpers.UiAutomatorHelpers

object LoginSteps {

    val usernameField=":LoginScreen:enter_username"
    val passwordField=":LoginScreen:enter_password"
    val errorField=":LoginScreen:login_error"
    val loginButton=":LoginScreen:login_button"
    val vpnProfileOkButton=":VpnPermissionScreen:ok"
    val androidOkButton="android:id/button1"
    val appAllowNotifications=":NotificationPermissionScreen:notifications_action"
    val androidAllowNotifications="com.android.permissioncontroller:id/permission_allow_button"

    fun navigateToLoginScreen() {
        UiAutomatorHelpers.clickWhenReady(SignUpSteps.loginButton)

//        waitUntilVisible(usernameField)
    }

    fun logIn(username: String, password: String) {
//        waitUntilVisible(usernameField)
        UiAutomatorHelpers.inputTextWhenReady(usernameField, username)
        UiAutomatorHelpers.inputTextWhenReady(passwordField, password)
        UiAutomatorHelpers.clickWhenReady(loginButton)
    }

    fun allowVpnProfileCreation() {
        UiAutomatorHelpers.findByResId(vpnProfileOkButton).takeIf { it != null }?.let {
            UiAutomatorHelpers.findByResId(vpnProfileOkButton)?.click()
            UiAutomatorHelpers.findByResId(androidOkButton)?.click()
        }
    }

    fun allowNotifications() {
        UiAutomatorHelpers.findByResId(appAllowNotifications).takeIf { it != null }?.let {
            UiAutomatorHelpers.findByResId(appAllowNotifications)?.click()
            UiAutomatorHelpers.findByResId(androidAllowNotifications)?.click()
        }
    }

    fun giveAppPermissions() {
        allowVpnProfileCreation()
        allowNotifications()
    }
}
