package com.kape.vpnconnect.platformsdk

import android.content.Context
import android.util.Log
import com.kape.platformsdk.vpn.service.VpnServiceLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ServiceLogger(
    private val context: Context,
    private val tag: VpnServiceLoggerTag,
) : VpnServiceLogger {
    override fun trace(message: String) = log("V", message)

    override fun debug(message: String) = log("D", message)

    override fun info(message: String) = log("I", message)

    override fun warning(message: String) = log("W", message)

    override fun error(message: String) = log("E", message)

    private fun log(
        level: String,
        message: String,
    ) {
        Log.v(tag.prefix, message)
        ServiceLogFileStore.append(context, level, tag.prefix, message)
    }

    suspend fun getLogs(): List<String> =
        withContext(Dispatchers.IO) {
            ServiceLogFileStore.readAll(context)
        }

    suspend fun clearLogs() =
        withContext(Dispatchers.IO) {
            ServiceLogFileStore.clear(context)
        }

    sealed class VpnServiceLoggerTag(
        val prefix: String,
    ) {
        data object OpenVpn : VpnServiceLoggerTag("OpenVPN")

        data object WireGuard : VpnServiceLoggerTag("WireGuard")

        data object Automatic : VpnServiceLoggerTag("Automatic")
    }
}

// System logcat is a small ring buffer shared with every other app and process on the device, so
// VPN debug lines routinely get evicted by unrelated log noise long before a user opens the log
// screen or sends a support report. This keeps its own bounded copy on disk instead: a single
// backup file is rotated in so total usage never exceeds twice MAX_LOG_FILE_SIZE_BYTES.
internal object ServiceLogFileStore {
    private const val MAX_LOG_FILE_SIZE_BYTES = 1L * 1024 * 1024
    private const val LOG_FILE_NAME = "vpn_debug.log"
    private const val LOG_FILE_BACKUP_NAME = "vpn_debug.log.old"

    private val lock = Any()

    fun append(
        context: Context,
        level: String,
        prefix: String,
        message: String,
    ) {
        synchronized(lock) {
            val current = currentFile(context)
            if (current.length() >= MAX_LOG_FILE_SIZE_BYTES) rotate(context, current)
            current.appendText("${timestamp()} $level/$prefix: $message\n")
        }
    }

    fun readAll(context: Context): List<String> =
        synchronized(lock) {
            backupFile(context).takeIf(File::exists)?.readLines().orEmpty() +
                currentFile(context).takeIf(File::exists)?.readLines().orEmpty()
        }

    fun clear(context: Context) =
        synchronized(lock) {
            currentFile(context).delete()
            backupFile(context).delete()
            Unit
        }

    private fun rotate(
        context: Context,
        current: File,
    ) {
        val backup = backupFile(context)
        backup.delete()
        current.renameTo(backup)
    }

    private fun timestamp(): String = SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.US).format(Date())

    private fun logDir(context: Context): File = File(context.filesDir, "logs").apply { mkdirs() }

    private fun currentFile(context: Context): File = File(logDir(context), LOG_FILE_NAME)

    private fun backupFile(context: Context): File = File(logDir(context), LOG_FILE_BACKUP_NAME)
}