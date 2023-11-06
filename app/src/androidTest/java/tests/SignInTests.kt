package com.privateinternetaccess.android.tests

import com.kape.vpn.BuildConfig
import com.privateinternetaccess.android.core.BaseUiAutomatorClass
import com.privateinternetaccess.android.helpers.ActionHelpers.giveAppPermissions
import com.privateinternetaccess.android.helpers.ActionHelpers.trySignIn
import com.privateinternetaccess.android.screens.objects.MainScreenPageObjects
import com.privateinternetaccess.android.screens.objects.SignIn
import org.junit.Test

class SignInTests() : BaseUiAutomatorClass() {

    @Test
    fun sign_in_with_valid_credentials_reaches_connect_screen() {
        trySignIn("${BuildConfig.PIA_VALID_USERNAME}", "${BuildConfig.PIA_VALID_PASSWORD}")
        giveAppPermissions()
        assert(MainScreenPageObjects.connectButton.exists())
    }

    @Test
    fun sign_in_with_invalid_credentials_does_not_login() {
        trySignIn("InvalidUser", "InvalidPassword")
        assert(SignIn.loginButton.exists())
    }

    @Test
    fun sign_in_empty_user_and_password_does_not_login() {
        trySignIn("", "")
        assert(SignIn.loginButton.exists())
    }

    @Test
    fun sign_in_valid_user_empty_password_does_not_login() {
        trySignIn("${BuildConfig.PIA_VALID_USERNAME}", "")
        assert(SignIn.loginButton.exists())
    }

    @Test
    fun sign_in_empty_user_valid_password_does_not_login() {
        trySignIn("", "${BuildConfig.PIA_VALID_PASSWORD}")
        assert(SignIn.loginButton.exists())
    }
}