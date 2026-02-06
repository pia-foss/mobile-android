package screens.steps

import androidx.test.uiautomator.UiAutomatorTestScope
import screens.helpers.UiAutomatorHelpers.click

object SettingsSteps {
    const val PROTOCOLS_BUTTON = ":SettingsScreen:Protocols"

    fun UiAutomatorTestScope.navigateToSettingsPage() {
        click(MainScreenSteps.SIDE_MENU)
        click(SideMenuSteps.SETTINGS_BUTTON)
    }
}
