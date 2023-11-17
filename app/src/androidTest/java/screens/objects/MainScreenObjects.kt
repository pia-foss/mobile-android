package screens.objects

object MainScreenPageObjects {
    val connectButton =
        UiAutomatorObjectFinder.findByResourceId(":ConnectionScreen:connection_button")

    val hamburgerMenu =
        UiAutomatorObjectFinder.findByResourceId(":AppBar:HamburgerMenu")
}