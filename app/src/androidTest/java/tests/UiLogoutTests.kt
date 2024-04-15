package tests

import com.kape.settings.data.VpnProtocols
import com.kape.vpn.BuildConfig
import org.junit.Test
import screens.objects.SignUpUiObjects
import kotlin.test.assertEquals

class UiLogoutTests : UiTest() {
    @Test
    fun sign_out_from_connect_screen_reaches_signup_screen() {
        signUpUiObjects.navigateToSignUpScreen()
        loginUiObjects.navigateToLoginScreen()
        loginUiObjects.logIn(BuildConfig.PIA_VALID_USERNAME, BuildConfig.PIA_VALID_PASSWORD)
        loginUiObjects.giveAppPermissions()
        mainScreenUiObjects.navigateToMainScreen()
        sideMenuUiObjects.logOut()
        assert(signUpUiObjects.loginButton.exists())
    }

//    @Test
//    fun persistence_layer_wiped_after_sign_out() {
//        signUpUiObjects.navigateToSignUpScreen()
//        loginUiObjects.navigateToLoginScreen()
//        loginUiObjects.logIn(BuildConfig.PIA_VALID_USERNAME, BuildConfig.PIA_VALID_PASSWORD)
//        loginUiObjects.giveAppPermissions()
//        uiSettingsAction.openSideMenu()
//        uiSettingsAction.selectOpenVpnProtocol()
//        uiLogoutAction.logout()
//        uiSignInAction.signIn(
//            BuildConfig.PIA_VALID_USERNAME,
//            BuildConfig.PIA_VALID_PASSWORD,
//        )
//        uiSettingsAction.openSideMenu()
//        // WireGuard is the default protocol. On logout we clear all settings
//        // If the protocol is set back to WireGuard it means that the persistence layer
//        // was successfully cleared
//        assertEquals(VpnProtocols.WireGuard.name, uiSettingsAction.selectedProtocol())
//    }
}
