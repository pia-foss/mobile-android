package tests.actions

import screens.steps.LogOutSteps
import screens.steps.SignInSteps

class UiAction(
    private val signInSteps: SignInSteps,
    private val logOutSteps: LogOutSteps,
) {

    fun signIn(username: String, password: String) {
        signInSteps.navigateToSignUpScreen()
        signInSteps.clickOnLoginButtonSignUpScreen()
        signInSteps.enterCredentials(username, password)
        signInSteps.clickOnLoginButtonLoginScreen()
    }

    fun giveAppPermissions() {
        signInSteps.allowVpnProfileCreation()
        signInSteps.allowNotifications()
    }

    fun logout() {
        logOutSteps.goToSettings()
        logOutSteps.clickOnLogoutSettingsButton()
    }
}