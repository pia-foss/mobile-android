package com.kape.router

const val NavigateBack = "back"

object Login {
    const val Main = "login-with-credentials"
    const val WithEmail = "login-with-email"
}

object VpnPermission {
    const val Main = "request-vpn-permission"
}

object Splash {
    const val Main = "splash-screen"
}

object Connection {
    const val Main = "connection-screen"
}

object RegionSelection {
    const val Main = "region-selection-screen"
}

object Profile {
    const val Main = "profile-screen"
}

object Subscribe {
    const val Main = "subscription-screen"
}

object Settings {
    const val Main = "settings-screen"
    const val General = "settings-general-screen"
    const val Protocols = "settings-protocols-screen"
    const val Networks = "settings-networks-screen"
    const val Privacy = "settings-privacy-screen"
    const val Automation = "settings-automation-screen"
}

object WebContent {
    // Please note these values are links within the signup module's strings file.
    const val Terms = "web-screen-terms"
    const val Privacy = "web-screen-privacy"
    const val Survey = "web-screen-survey"
}