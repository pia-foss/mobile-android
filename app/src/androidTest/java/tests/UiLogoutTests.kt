package tests

import com.kape.settings.data.VpnProtocols
import com.kape.vpn.BuildConfig
import org.junit.Test
import kotlin.test.assertEquals

class UiLogoutTests : UiTest() {
    @Test
    fun sign_out_from_connect_screen_reaches_signup_screen() {
        sideMenuSteps.navigateToSideMenu()
        sideMenuSteps.logOut()
        assert(signUpSteps.loginButton.exists())
    }

    @Test
    fun persistence_layer_wiped_after_sign_out() {
        settingsSteps.navigateToSettingsPage()
        protocolSteps.selectProtocol()
        protocolSteps.selectOpenVPN()
        mainScreenSteps.navigateToMainScreen()
        sideMenuSteps.navigateToSideMenu()
        sideMenuSteps.logOut()
        signUpSteps.navigateToSignUpScreen()
        loginSteps.navigateToLoginScreen()
        loginSteps.logIn(BuildConfig.PIA_VALID_USERNAME, BuildConfig.PIA_VALID_PASSWORD)
        loginSteps.giveAppPermissions()
        settingsSteps.navigateToSettingsPage()
        // WireGuard is the default protocol. On logout we clear all settings
        // If the protocol is set back to WireGuard it means that the persistence layer
        // was successfully cleared
        assertEquals(VpnProtocols.WireGuard.name, protocolSteps.getSelectedProtocol())
    }
}
