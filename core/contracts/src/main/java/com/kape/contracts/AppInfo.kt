package com.kape.contracts

interface AppInfo {
    val buildFlavor: String
    val buildType: String
    val versionName: String
    val versionCode: Int
}