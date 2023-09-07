package com.kape.vpnconnect.provider

import androidx.compose.runtime.mutableStateOf
import com.kape.vpnmanager.presenters.VPNManagerProtocolByteCountDependency

class UsageProvider : VPNManagerProtocolByteCountDependency {

    val download = mutableStateOf(humanReadableByteCountSI(0))
    val upload = mutableStateOf(humanReadableByteCountSI(0))

    override fun byteCount(tx: Long, rx: Long) {
        download.value = humanReadableByteCountSI(rx)
        upload.value = humanReadableByteCountSI(tx)
    }

    fun reset() {
        download.value = humanReadableByteCountSI(0)
        upload.value = humanReadableByteCountSI(0)
    }

    private fun humanReadableByteCountSI(bytes: Long): String {
        val s = if (bytes < 0) "-" else ""
        var b = if (bytes == Long.MIN_VALUE) Long.MAX_VALUE else Math.abs(bytes)
        return if (b < 1000L) "$bytes B" else if (b < 999950L) String.format(
            "%s%.1f kB",
            s,
            b / 1e3,
        ) else if (1000.let { b /= it; b } < 999950L) String.format(
            "%s%.1f MB",
            s,
            b / 1e3,
        ) else if (1000.let { b /= it; b } < 999950L) String.format(
            "%s%.1f GB",
            s,
            b / 1e3,
        ) else if (1000.let { b /= it; b } < 999950L) String.format(
            "%s%.1f TB",
            s,
            b / 1e3,
        ) else if (1000.let { b /= it; b } < 999950L) String.format(
            "%s%.1f PB",
            s,
            b / 1e3,
        ) else String.format("%s%.1f EB", s, b / 1e6)
    }
}