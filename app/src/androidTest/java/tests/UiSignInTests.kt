package tests

import com.kape.vpn.BuildConfig
import org.junit.Test
import screens.objects.LoginUiObjects
import screens.objects.MainScreenPageObjects

class UiSignInTests : UiTest() {

    @Test
    fun sign_in_with_valid_credentials_reaches_connect_screen() {
        uiAction.signIn("${BuildConfig.PIA_VALID_USERNAME}", "${BuildConfig.PIA_VALID_PASSWORD}")
        uiAction.giveAppPermissions()
        assert(MainScreenPageObjects.connectButton.exists())
    }

    @Test
    fun sign_in_with_invalid_credentials_does_not_login() {
        uiAction.signIn("InvalidUser", "InvalidPassword")
        assert(LoginUiObjects.loginButton.exists())
    }

    @Test
    fun sign_in_empty_user_and_password_does_not_login() {
        uiAction.signIn("", "")
        assert(LoginUiObjects.loginButton.exists())
    }

    @Test
    fun sign_in_valid_user_empty_password_does_not_login() {
        uiAction.signIn("${BuildConfig.PIA_VALID_USERNAME}", "")
        assert(LoginUiObjects.loginButton.exists())
    }

    @Test
    fun sign_in_empty_user_valid_password_does_not_login() {
        uiAction.signIn("", "${BuildConfig.PIA_VALID_PASSWORD}")
        assert(LoginUiObjects.loginButton.exists())
    }
}