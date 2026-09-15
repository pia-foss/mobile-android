package com.kape.contracts

interface AppInfo {
    val buildFlavor: String
    val buildType: String
    val versionName: String
    val versionCode: Int

    /**
     * True only for the very first install of this package on this device (firstInstallTime ==
     * lastUpdateTime) — false for any device that has ever been upgraded from an earlier version.
     */
    val isFreshInstall: Boolean
}