package tests.actions

import screens.steps.SignInSteps

class UiAction(private val signInStep: SignInSteps) {

    fun signIn(username: String, password: String) {
        signInStep.navigateToSignUpScreen()
        signInStep.clickOnLoginButtonSignUpScreen()
        signInStep.enterCredentials(username, password)
        signInStep.clickOnLoginButtonLoginScreen()
    }

    fun giveAppPermissions() {
        signInStep.allowVpnProfileCreation()
        signInStep.allowNotifications()
    }
}