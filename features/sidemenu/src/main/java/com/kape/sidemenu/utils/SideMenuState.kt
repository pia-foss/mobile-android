package com.kape.sidemenu.utils

data class SideMenuState(
    val username: String,
    val versionCode: Int,
    val versionName: String,
    val showExpirationNotice: Boolean,
    val daysRemaining: Int
)

val IDLE = SideMenuState("", 0, "", false, 0)