package tests.actions

import screens.steps.interfaces.SignInSteps

class UiSignInAction(private val signInSteps: SignInSteps) {
    fun signIn(username: String, password: String) {
        signInSteps.navigateToSignUpScreen()
        signInSteps.clickOnLoginButtonSignUpScreen()
        signInSteps.enterCredentials(username, password)
        signInSteps.clickOnLoginButtonLoginScreen()
        giveAppPermissions()
    }

    private fun giveAppPermissions() {
        signInSteps.allowVpnProfileCreation()
        signInSteps.allowNotifications()
    }
}