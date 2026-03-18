package screens.steps

import androidx.test.uiautomator.UiAutomatorTestScope
import screens.helpers.UiAutomatorHelpers.click
import screens.helpers.UiAutomatorHelpers.findByResId

object SettingsSteps {
    const val PROTOCOLS_BUTTON = ":SettingsScreen:Protocols"

    fun UiAutomatorTestScope.navigateToSettingsPage() {
        click(MainScreenSteps.SIDE_MENU)
        findByResId(SideMenuSteps.SETTINGS_BUTTON)
        click(SideMenuSteps.SETTINGS_BUTTON)
    }
}
