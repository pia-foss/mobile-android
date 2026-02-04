package tests

import androidx.test.uiautomator.uiAutomator
import com.kape.settings.data.VpnProtocols
import com.kape.vpn.BuildConfig
import org.junit.Test
import screens.helpers.UiAutomatorHelpers.findByPartialText
import screens.helpers.UiAutomatorHelpers.findByResId
import screens.helpers.UiAutomatorHelpers.pressBackTwice
import screens.steps.LoginSteps.logIn
import screens.steps.LoginSteps.navigateToLoginScreen
import screens.steps.ProtocolsSteps.selectOpenVPN
import screens.steps.ProtocolsSteps.selectProtocol
import screens.steps.SettingsSteps
import screens.steps.SettingsSteps.navigateToSettingsPage
import screens.steps.SideMenuSteps.logOut
import screens.steps.SideMenuSteps.navigateToSideMenu
import screens.steps.SignUpSteps
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class UiLogoutTests : UiTest() {

    @Test
    fun sign_out_from_connect_screen_reaches_signup_screen() = uiAutomator {
        navigateToSideMenu()
        logOut()

        assertNotNull(findByResId(SignUpSteps.LOGIN_BUTTON))
    }

    @Test
    fun persistence_layer_wiped_after_sign_out() = uiAutomator {
        navigateToSettingsPage()
        selectProtocol()
        selectOpenVPN()

        pressBackTwice(2)

        navigateToSideMenu()
        logOut()

        navigateToLoginScreen()
        logIn(BuildConfig.PIA_VALID_USERNAME, BuildConfig.PIA_VALID_PASSWORD)

        navigateToSettingsPage()
        assertNotNull(findByResId(SettingsSteps.PROTOCOLS_BUTTON))
        assertNotNull(findByPartialText(VpnProtocols.WireGuard.name))
        assertTrue(true)
    }
}
