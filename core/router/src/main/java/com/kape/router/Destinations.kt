package com.kape.router

const val NavigateBack = "back"

object Login {
    const val Route = "login"
    const val WithCredentials = "login-with-credentials"
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
    const val Help = "settings-help-screen"
    const val KillSwitch = "settings-kill-switch"
    const val QuickSettings = "settings-quick-settings"
    const val ConnectionStats = "settings-connection-stats"
    const val DebugLogs = "settings-debug-logs"
    const val Widget = "settings-widget"
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
}