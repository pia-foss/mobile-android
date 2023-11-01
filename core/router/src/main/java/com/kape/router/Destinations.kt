package com.kape.router

const val NavigateBack = "back"
const val NavigateOut = "exit"

object Login {
    const val Route = "login"
    const val WithCredentials = "login-with-credentials"
    const val WithEmail = "login-with-email"
}

object VpnPermission {
    const val Main = "request-vpn-permission"
}

object NotificationPermission {
    const val Main = "request-notification-permission"
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
    const val Route = "settings"
    const val Main = "settings-screen"
    const val Automation = "settings-automation-screen"
    const val KillSwitch = "settings-kill-switch"
    const val QuickSettings = "settings-quick-settings"
}

object PerAppSettings {
    const val Main = "per-app-settings-screen"
}

object DedicatedIp {
    const val Main = "dedicated-ip"
}

object WebContent {
    // Please note these values are links within the signup module's strings file.
    const val Terms = "web-screen-terms"
    const val Privacy = "web-screen-privacy"
    const val Survey = "web-screen-survey"
    const val Support = "web-screen-support"
}