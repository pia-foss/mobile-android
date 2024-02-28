package com.kape.router

const val NavigateBack = "back"
const val NavigateOut = "exit"

object Login {
    const val Route = "login"
    const val WithCredentials = "login-with-credentials"
    const val WithEmail = "login-with-email"
}

object Permissions {
    const val Route = "permissions"
}

object Splash {
    const val Main = "splash-screen"
}

object TvWelcome {
    const val Main = "tv-welcome-screen"
}

object Connection {
    const val Main = "connection-screen"
}

object VpnRegionSelection {
    const val Main = "region-selection-screen"
}

object ShadowsocksRegionSelection {
    const val Main = "shadowsocks-region-selection-screen"
}

object Profile {
    const val Main = "profile-screen"
}

object Subscribe {
    const val Main = "subscription-screen"
}

object Settings {
    const val Route = "settings"
    const val Main = "settings-screen"
    const val Automation = "settings-automation-screen"
    const val KillSwitch = "settings-kill-switch"
    const val Protocols = "settings-protocol"
}

object About {
    const val Main = "about-screen"
}

object PerAppSettings {
    const val Main = "per-app-settings-screen"
}

object DedicatedIp {
    const val Main = "dedicated-ip"
}

object Automation {
    const val Route = "automation"
}

object Customization {
    const val Route = "customization"
}

object WebContent {
    // Please note these values are links within the signup module's strings file.
    const val Terms = "web-screen-terms"
    const val Privacy = "web-screen-privacy"
    const val Support = "web-screen-support"
}