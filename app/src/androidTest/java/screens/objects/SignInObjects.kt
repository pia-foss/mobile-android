package com.privateinternetaccess.android.screens.objects

import com.privateinternetaccess.android.helpers.LocatorHelper

object SignIn {
    val reachLoginScreenButton = LocatorHelper.findByResourceId(":SignUpScreen:Login")
    val usernameField = LocatorHelper.findByResourceId(":LoginScreen:enter_username")
    val passwordField = LocatorHelper.findByResourceId(":LoginScreen:enter_password")
    val loginButton = LocatorHelper.findByResourceId(":LoginScreen:login_button")
    val vpnProfileOkButton = LocatorHelper.findByResourceId(":VpnPermissionScreen:ok")
    val androidOkButton = LocatorHelper.findByResourceId("android:id/button1")
    val appAllowNotifications = LocatorHelper.findByResourceId(":NotificationPermissionScreen:notifications_action")
    val androidAllowNotifications = LocatorHelper.findByResourceId("com.android.permissioncontroller:id/permission_allow_button")
}