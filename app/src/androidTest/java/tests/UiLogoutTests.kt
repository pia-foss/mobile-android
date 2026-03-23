package tests

import androidx.test.uiautomator.uiAutomator
import com.kape.settings.data.VpnProtocols
import com.kape.vpn.BuildConfig
import org.junit.Test
import screens.helpers.UiAutomatorHelpers.LONG_TIMEOUT
import screens.helpers.UiAutomatorHelpers.findByPartialText
import screens.helpers.UiAutomatorHelpers.findByResId
import screens.helpers.UiAutomatorHelpers.pressBackTwice
import screens.steps.LoginSteps.giveAppPermissions
import screens.steps.LoginSteps.logIn
import screens.steps.LoginSteps.navigateToLoginScreen
import screens.steps.ProtocolsSteps.selectOpenVPN
import screens.steps.ProtocolsSteps.selectProtocol
import screens.steps.SettingsSteps.navigateToSettingsPage
import screens.steps.SideMenuSteps.logOut
import screens.steps.SideMenuSteps.navigateToSideMenu
import screens.steps.SignUpSteps
import kotlin.test.assertNotNull

class UiLogoutTests : UiTest() {

    @Test
    fun sign_out_from_connect_screen_reaches_signup_screen() = uiAutomator {
        setupWithFreshLogin()
        navigateToSideMenu()
        logOut()

        assertNotNull(findByResId(SignUpSteps.LOGIN_BUTTON, LONG_TIMEOUT))
    }

    @Test
    fun persistence_layer_wiped_after_sign_out() = uiAutomator {
        setupWithFreshLogin()
        navigateToSettingsPage()
        selectProtocol()
        selectOpenVPN()

        pressBackTwice(2)

        navigateToSideMenu()
        logOut()

        navigateToLoginScreen()
        logIn(BuildConfig.PIA_VALID_USERNAME, BuildConfig.PIA_VALID_PASSWORD)
        giveAppPermissions()

        navigateToSettingsPage()
        assertNotNull(findByPartialText(VpnProtocols.WireGuard.name))
    }
}
