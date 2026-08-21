package com.kape.vpnconnect.platformsdk

import android.util.Log
import com.kape.platformsdk.vpn.service.VpnServiceLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ServiceLogger(
    val tag: VpnServiceLoggerTag,
) : VpnServiceLogger {
    override fun trace(message: String) {
        Log.v(tag.prefix, message)
    }

    override fun debug(message: String) {
        Log.v(tag.prefix, message)
    }

    override fun info(message: String) {
        Log.v(tag.prefix, message)
    }

    override fun warning(message: String) {
        Log.v(tag.prefix, message)
    }

    override fun error(message: String) {
        Log.v(tag.prefix, message)
    }

    suspend fun getLogs(): List<String> =
        withContext(Dispatchers.IO) {
            val process =
                ProcessBuilder(
                    "logcat",
                    "-b",
                    "all",
                    "-t",
                    "2000",
                    "-d",
                    "-v",
                    "threadtime",
                    "${tag.prefix}:V",
                    "*:S",
                ).redirectErrorStream(true)
                    .start()
            process.inputStream.bufferedReader().useLines { it.toList() }
        }

    sealed class VpnServiceLoggerTag(
        val prefix: String,
    ) {
        data object OpenVpn : VpnServiceLoggerTag("OpenVPN")

        data object WireGuard : VpnServiceLoggerTag("WireGuard")

        data object Automatic : VpnServiceLoggerTag("Automatic")
    }
}