package com.privateinternetaccess.android.screens.objects

import com.privateinternetaccess.android.helpers.LocatorHelper

class ProtocolPageObjects {

    val protocolSettings = LocatorHelper.findByResourceId("com.privateinternetaccess.android:id/protocolsSettings")
    val protocolSelection = LocatorHelper.findByResourceId("com.privateinternetaccess.android:id/protocolSetting")
    val protocolSummarySetting = LocatorHelper.findByResourceId("com.privateinternetaccess.android:id/protocolSummarySetting")
    val openVPN = LocatorHelper.findByText("OpenVPN")
    val wireGuard = LocatorHelper.findByText("WireGuard")
    val smallPacketsSwitchSetting = LocatorHelper.findByResourceId("com.privateinternetaccess.android:id/smallPacketsSwitchSetting")
    val save = LocatorHelper.findByResourceId("android:id/button1")
}