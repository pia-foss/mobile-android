package com.kape.settings.utils

import android.content.Context
import com.kape.utils.Prefs

private const val LAUNCH_ON_STARTUP = "launch-on-startup"
private const val CONNECT_ON_LAUNCH = "connect-on-launch"
private const val CONNECT_ON_APP_UPDATE = "connect-on-app-update"
class SettingsPrefs(context: Context) : Prefs(context, "settings") {

    fun enableLaunchOnStartup(enable: Boolean) {
        prefs.edit().putBoolean(LAUNCH_ON_STARTUP, enable).apply()
    }

    fun isLaunchOnStartupEnabled(): Boolean {
        return prefs.getBoolean(LAUNCH_ON_STARTUP, false)
    }

    fun enableConnectOnLaunch(enable: Boolean) {
        prefs.edit().putBoolean(CONNECT_ON_LAUNCH, enable).apply()
    }

    fun isConnectOnLaunchEnabled(): Boolean {
        return prefs.getBoolean(CONNECT_ON_LAUNCH, false)
    }

    fun enableConnectOnAppUpdate(enable: Boolean) {
        prefs.edit().putBoolean(CONNECT_ON_APP_UPDATE, enable).apply()
    }

    fun isConnectOnAppUpdateEnabled(): Boolean {
        return prefs.getBoolean(CONNECT_ON_APP_UPDATE, false)
    }
}