package tests

import com.kape.vpn.BuildConfig
import org.junit.Test
import screens.steps.LoginSteps
import screens.steps.MainScreenSteps

class UiSignInTests : UiTest() {
    @Test
    fun sign_in_with_valid_credentials_reaches_connect_screen() {
        signUpSteps.navigateToSignUpScreen()
        loginSteps.navigateToLoginScreen()
        loginSteps.logIn(BuildConfig.PIA_VALID_USERNAME, BuildConfig.PIA_VALID_PASSWORD)
        loginSteps.giveAppPermissions()
        assert(MainScreenSteps.connectButton.exists())
    }

    @Test
    fun sign_in_with_invalid_credentials_does_not_login() {
        signUpSteps.navigateToSignUpScreen()
        loginSteps.navigateToLoginScreen()
        loginSteps.logIn("InvalidUser", "InvalidPassword")
        loginSteps.giveAppPermissions()
        assert(LoginSteps.loginButton.exists())
    }

    @Test
    fun sign_in_empty_user_and_password_does_not_login() {
        signUpSteps.navigateToSignUpScreen()
        loginSteps.navigateToLoginScreen()
        loginSteps.logIn("", "")
        loginSteps.giveAppPermissions()
        assert(LoginSteps.loginButton.exists())
    }

    @Test
    fun sign_in_valid_user_empty_password_does_not_login() {
        signUpSteps.navigateToSignUpScreen()
        loginSteps.navigateToLoginScreen()
        loginSteps.logIn(BuildConfig.PIA_VALID_USERNAME, "")
        loginSteps.giveAppPermissions()
        assert(LoginSteps.loginButton.exists())
    }

    @Test
    fun sign_in_empty_user_valid_password_does_not_login() {
        signUpSteps.navigateToSignUpScreen()
        loginSteps.navigateToLoginScreen()
        loginSteps.logIn("", BuildConfig.PIA_VALID_PASSWORD)
        loginSteps.giveAppPermissions()
        assert(LoginSteps.loginButton.exists())
    }
}
