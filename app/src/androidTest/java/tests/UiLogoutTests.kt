package tests

import androidx.test.uiautomator.uiAutomator
import org.junit.Test
import screens.helpers.UiAutomatorHelpers.get
import screens.helpers.UiAutomatorHelpers.launchAppAndLogIn
import screens.helpers.UiAutomatorHelpers.logIn
import screens.helpers.UiAutomatorHelpers.waitForElement
import screens.steps.ProtocolsSteps
import screens.steps.ProtocolsSteps.selectOpenVPN
import screens.steps.ProtocolsSteps.selectProtocol
import screens.steps.SideMenuSteps.logout
import screens.steps.SideMenuSteps.navigateToSettings
import screens.steps.SignUpSteps
import kotlin.test.assertTrue

class UiLogoutTests {

    @Test
    fun sign_out_from_connect_screen_reaches_signup_screen() = uiAutomator {
        launchAppAndLogIn()
        logout()

        assertTrue(waitForElement(SignUpSteps.SIGNUP_LOGIN_BUTTON))
    }

    @Test
    fun persistence_layer_wiped_after_sign_out() = uiAutomator {
        launchAppAndLogIn()
        navigateToSettings()
        selectProtocol()
        selectOpenVPN()
        // get back to main screen
        pressBack()
        pressBack()
        logout()

        logIn()
        navigateToSettings()
        selectProtocol()
        waitForElement(ProtocolsSteps.WIRE_GUARD_BUTTON)
        assertTrue(get(ProtocolsSteps.WIRE_GUARD_BUTTON).isEnabled)
    }
}
