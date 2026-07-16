package com.kape.vpnconnect.platformsdk

import android.util.Log
import com.kape.platformsdk.vpn.service.VpnServiceLogger

class KapeLogger(
    private val subsystem: String,
    private val category: String,
) : VpnServiceLogger {
    private val tag = "--- $subsystem/$category"

    override fun trace(message: String) {
        Log.v(tag, message)
    }

    override fun debug(message: String) {
        Log.d(tag, message)
    }

    override fun info(message: String) {
        Log.i(tag, message)
    }

    override fun warning(message: String) {
        Log.w(tag, message)
    }

    override fun error(message: String) {
        Log.e(tag, message)
    }
}