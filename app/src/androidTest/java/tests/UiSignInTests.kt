package tests

import com.kape.vpn.BuildConfig
import org.junit.Test
import screens.objects.LoginUiObjects
import screens.objects.MainScreenObjects

class UiSignInTests : UiTest() {
    @Test
    fun sign_in_with_valid_credentials_reaches_connect_screen() {
        signUpUiObjects.navigateToSignUpScreen()
        loginUiObjects.navigateToLoginScreen()
        loginUiObjects.logIn(BuildConfig.PIA_VALID_USERNAME, BuildConfig.PIA_VALID_PASSWORD)
        loginUiObjects.giveAppPermissions()
        assert(MainScreenObjects.connectButton.exists())
    }

    @Test
    fun sign_in_with_invalid_credentials_does_not_login() {
        signUpUiObjects.navigateToSignUpScreen()
        loginUiObjects.navigateToLoginScreen()
        loginUiObjects.logIn("InvalidUser", "InvalidPassword")
        loginUiObjects.giveAppPermissions()
        assert(LoginUiObjects.loginButton.exists())
    }

    @Test
    fun sign_in_empty_user_and_password_does_not_login() {
        signUpUiObjects.navigateToSignUpScreen()
        loginUiObjects.navigateToLoginScreen()
        loginUiObjects.logIn("", "")
        loginUiObjects.giveAppPermissions()
        assert(LoginUiObjects.loginButton.exists())
    }

    @Test
    fun sign_in_valid_user_empty_password_does_not_login() {
        signUpUiObjects.navigateToSignUpScreen()
        loginUiObjects.navigateToLoginScreen()
        loginUiObjects.logIn(BuildConfig.PIA_VALID_USERNAME, "")
        loginUiObjects.giveAppPermissions()
        assert(LoginUiObjects.loginButton.exists())
    }

    @Test
    fun sign_in_empty_user_valid_password_does_not_login() {
        signUpUiObjects.navigateToSignUpScreen()
        loginUiObjects.navigateToLoginScreen()
        loginUiObjects.logIn("", BuildConfig.PIA_VALID_PASSWORD)
        loginUiObjects.giveAppPermissions()
        assert(LoginUiObjects.loginButton.exists())
    }
}
