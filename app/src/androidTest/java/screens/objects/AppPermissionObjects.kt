package screens.objects

object AppPermissionObjects {
    val vpnProfileOkButton = UiAutomatorObjectFinder.findByResourceId(":VpnPermissionScreen:ok")
    val androidOkButton = UiAutomatorObjectFinder.findByResourceId("android:id/button1")
    val appAllowNotifications =
        UiAutomatorObjectFinder.findByResourceId(":NotificationPermissionScreen:notifications_action")
    val androidAllowNotifications =
        UiAutomatorObjectFinder.findByResourceId("com.android.permissioncontroller:id/permission_allow_button")
}