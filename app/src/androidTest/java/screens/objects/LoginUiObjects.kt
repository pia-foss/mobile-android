package screens.objects

import screens.helpers.UiAutomatorObjectFinder

object LoginUiObjects {
    val usernameField = UiAutomatorObjectFinder.findByResourceId(":LoginScreen:enter_username")
    val passwordField = UiAutomatorObjectFinder.findByResourceId(":LoginScreen:enter_password")
    val loginButton = UiAutomatorObjectFinder.findByResourceId(":LoginScreen:login_button")
}
