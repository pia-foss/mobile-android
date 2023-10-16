package com.kape.vpnconnect.provider

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import androidx.compose.runtime.mutableStateOf
import com.kape.vpnconnect.R
import com.kape.vpnmanager.presenters.VPNManagerProtocolByteCountDependency
import kotlin.math.ln
import kotlin.math.pow

class UsageProvider(private val context: Context, private val intents: List<Intent>) :
    VPNManagerProtocolByteCountDependency {

    val download = mutableStateOf(humanReadableByteCountSI(0))
    val upload = mutableStateOf(humanReadableByteCountSI(0))

    val widgetDownloadSpeed = mutableStateOf(humanReadableByteCount(0, true, context))
    val widgetDownload = mutableStateOf(humanReadableByteCount(0, false, context))
    val widgetUploadSpeed = mutableStateOf(humanReadableByteCount(0, true, context))
    val widgetUpload = mutableStateOf(humanReadableByteCount(0, false, context))

    override fun byteCount(tx: Long, rx: Long) {
        download.value = humanReadableByteCountSI(rx)
        upload.value = humanReadableByteCountSI(tx)
        widgetDownload.value = humanReadableByteCount(rx, false, context)
        widgetDownloadSpeed.value = humanReadableByteCount(rx, true, context)
        widgetUpload.value = humanReadableByteCount(tx, false, context)
        widgetUploadSpeed.value = humanReadableByteCount(tx, true, context)
        intents.forEach {
            context.sendBroadcast(it)
        }
    }

    fun reset() {
        download.value = humanReadableByteCountSI(0)
        upload.value = humanReadableByteCountSI(0)
        widgetUploadSpeed.value = humanReadableByteCount(0, true, context)
        widgetUpload.value = humanReadableByteCount(0, true, context)
        widgetDownload.value = humanReadableByteCount(0, true, context)
        widgetDownloadSpeed.value = humanReadableByteCount(0, true, context)
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

    fun humanReadableByteCount(bytes: Long, speed: Boolean, context: Context): String {
        var bytes = bytes / 2
        if (speed) bytes *= 8
        val unit = if (speed) 1000 else 1024
        val exp = 0.coerceAtLeast(
            (ln(bytes.toDouble()) / ln(unit.toDouble())).toInt().coerceAtMost(3),
        )
        val bytesUnit = (bytes / unit.toDouble().pow(exp.toDouble())).toFloat()
        return if (speed) when (exp) {
            0 -> context.getString(R.string.bits_per_second, bytesUnit)
            1 -> context.getString(R.string.kbits_per_second, bytesUnit)
            2 -> context.getString(R.string.mbits_per_second, bytesUnit)
            else -> context.getString(R.string.gbits_per_second, bytesUnit)
        } else when (exp) {
            0 -> context.getString(R.string.volume_byte, bytesUnit)
            1 -> context.getString(R.string.volume_kbyte, bytesUnit)
            2 -> context.getString(R.string.volume_mbyte, bytesUnit)
            else -> context.getString(R.string.volume_gbyte, bytesUnit)
        }
    }
}