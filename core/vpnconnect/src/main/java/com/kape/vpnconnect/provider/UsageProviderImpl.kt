package com.kape.vpnconnect.provider

import android.content.Context
import androidx.compose.runtime.snapshots.Snapshot
import com.kape.contracts.UsageProvider
import com.kape.vpnmanager.presenters.VPNManagerProtocolByteCountDependency
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.core.annotation.Singleton
import kotlin.math.ln
import kotlin.math.pow

@Singleton([UsageProvider::class, VPNManagerProtocolByteCountDependency::class])
class UsageProviderImpl(
    private val context: Context,
) : VPNManagerProtocolByteCountDependency,
    UsageProvider {
    override val download = MutableStateFlow(humanReadableByteCountSI(0))
    override val upload = MutableStateFlow(humanReadableByteCountSI(0))

    override val widgetDownloadSpeed = MutableStateFlow(humanReadableByteCount(0, true, context))
    override val widgetDownload = MutableStateFlow(humanReadableByteCount(0, false, context))
    override val widgetUploadSpeed = MutableStateFlow(humanReadableByteCount(0, true, context))
    override val widgetUpload = MutableStateFlow(humanReadableByteCount(0, false, context))

    override fun byteCount(
        tx: Long,
        rx: Long,
    ) {
        Snapshot.withMutableSnapshot {
            download.value = humanReadableByteCountSI(rx)
            upload.value = humanReadableByteCountSI(tx)
            widgetDownload.value = humanReadableByteCount(rx, false, context)
            widgetUpload.value = humanReadableByteCount(tx, false, context)
        }
        widgetDownloadSpeed.value = humanReadableByteCount(rx, true, context)
        widgetUploadSpeed.value = humanReadableByteCount(tx, true, context)
    }

    override fun reset() {
        Snapshot.withMutableSnapshot {
            download.value = humanReadableByteCountSI(0)
            upload.value = humanReadableByteCountSI(0)
            widgetUpload.value = humanReadableByteCount(0, false, context)
            widgetDownload.value = humanReadableByteCount(0, false, context)
        }
        widgetUploadSpeed.value = humanReadableByteCount(0, true, context)
        widgetDownloadSpeed.value = humanReadableByteCount(0, true, context)
    }

    private fun humanReadableByteCountSI(bytes: Long): String {
        val s = if (bytes < 0) "-" else ""
        var b = if (bytes == Long.MIN_VALUE) Long.MAX_VALUE else Math.abs(bytes)
        return if (b < 1000L) {
            "$bytes B"
        } else if (b < 999950L) {
            String.format(
                "%s%.1f kB",
                s,
                b / 1e3,
            )
        } else if (1000.let {
                b /= it
                b
            } < 999950L
        ) {
            String.format(
                "%s%.1f MB",
                s,
                b / 1e3,
            )
        } else if (1000.let {
                b /= it
                b
            } < 999950L
        ) {
            String.format(
                "%s%.1f GB",
                s,
                b / 1e3,
            )
        } else if (1000.let {
                b /= it
                b
            } < 999950L
        ) {
            String.format(
                "%s%.1f TB",
                s,
                b / 1e3,
            )
        } else if (1000.let {
                b /= it
                b
            } < 999950L
        ) {
            String.format(
                "%s%.1f PB",
                s,
                b / 1e3,
            )
        } else {
            String.format("%s%.1f EB", s, b / 1e6)
        }
    }

    private fun humanReadableByteCount(
        bytes: Long,
        speed: Boolean,
        context: Context,
    ): String {
        var bytes = bytes / 2
        if (speed) bytes *= 8
        val unit = if (speed) 1000 else 1024
        val exp =
            0.coerceAtLeast(
                (ln(bytes.toDouble()) / ln(unit.toDouble())).toInt().coerceAtMost(3),
            )
        val bytesUnit = (bytes / unit.toDouble().pow(exp.toDouble())).toFloat()
        return if (speed) {
            when (exp) {
                0 -> context.getString(com.kape.ui.R.string.bits_per_second, bytesUnit)
                1 -> context.getString(com.kape.ui.R.string.kbits_per_second, bytesUnit)
                2 -> context.getString(com.kape.ui.R.string.mbits_per_second, bytesUnit)
                else -> context.getString(com.kape.ui.R.string.gbits_per_second, bytesUnit)
            }
        } else {
            when (exp) {
                0 -> context.getString(com.kape.ui.R.string.volume_byte, bytesUnit)
                1 -> context.getString(com.kape.ui.R.string.volume_kbyte, bytesUnit)
                2 -> context.getString(com.kape.ui.R.string.volume_mbyte, bytesUnit)
                else -> context.getString(com.kape.ui.R.string.volume_gbyte, bytesUnit)
            }
        }
    }
}