package screens.steps.interfaces

interface SignInSteps {

    fun clickOnLoginButtonSignUpScreen()
    fun enterCredentials(username: String, password: String)
    fun clickOnLoginButtonLoginScreen()
    fun allowVpnProfileCreation()
    fun allowNotifications()
    fun navigateToSignUpScreen()
}