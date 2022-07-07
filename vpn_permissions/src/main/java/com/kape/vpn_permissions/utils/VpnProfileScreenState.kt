package com.kape.vpn_permissions.utils

data class VpnProfileState(val idle: Boolean, val requestProfile: Boolean, val granted: Boolean)

val IDLE = VpnProfileState(idle = true, requestProfile = false, granted = false)
val REQUEST = VpnProfileState(idle = false, requestProfile = true, granted = false)
val GRANTED = VpnProfileState(idle = false, requestProfile = false, granted = true)
val NOT_GRANTED = VpnProfileState(idle = false, requestProfile = false, granted = false)