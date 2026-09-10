package com.kape.vpnconnect.platformsdk

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class ServiceLoggerTest {
    @TempDir
    lateinit var filesDir: File

    private lateinit var context: Context

    @BeforeEach
    fun setUp() {
        context = mockk<Context>()
        every { context.filesDir } returns filesDir
    }

    @Test
    fun `getLogs - after writes on a fresh store - returns them in write order`() =
        runTest {
            val logger = ServiceLogger(context, ServiceLogger.VpnServiceLoggerTag.WireGuard)

            logger.trace("connecting")
            logger.info("connected")
            logger.error("tunnel dropped")

            val logs = logger.getLogs()

            assertEquals(3, logs.size)
            assertTrue(logs[0].contains("V/WireGuard: connecting"))
            assertTrue(logs[1].contains("I/WireGuard: connected"))
            assertTrue(logs[2].contains("E/WireGuard: tunnel dropped"))
        }

    @Test
    fun `getLogs - lines written by a different tag - are still visible, since the store is shared`() =
        runTest {
            val wireGuardLogger = ServiceLogger(context, ServiceLogger.VpnServiceLoggerTag.WireGuard)
            val openVpnLogger = ServiceLogger(context, ServiceLogger.VpnServiceLoggerTag.OpenVpn)

            wireGuardLogger.debug("first")
            openVpnLogger.debug("second")

            val logs = openVpnLogger.getLogs()

            assertEquals(2, logs.size)
            assertTrue(logs[0].contains("D/WireGuard: first"))
            assertTrue(logs[1].contains("D/OpenVPN: second"))
        }

    @Test
    fun `getLogs - current file grows past the size cap - rotates and keeps both old and new entries bounded`() =
        runTest {
            val logger = ServiceLogger(context, ServiceLogger.VpnServiceLoggerTag.Automatic)
            val bigMessage = "x".repeat(1024)

            // One MiB cap per file; ~1100 lines of 1 KiB each forces at least one rotation while
            // staying well clear of exhausting test time/disk.
            repeat(1100) { logger.debug(bigMessage) }
            logger.info("marker-after-rotation")

            val logDir = File(filesDir, "logs")
            val current = File(logDir, "vpn_debug.log")
            val backup = File(logDir, "vpn_debug.log.old")

            assertTrue(backup.exists(), "expected a backup file once the current file exceeded the cap")
            assertTrue(
                current.length() < backup.length(),
                "current file should have rotated to a fresh, small file rather than growing unbounded",
            )

            val logs = logger.getLogs()
            assertTrue(logs.last().contains("marker-after-rotation"))
        }

    @Test
    fun `clearLogs - after prior writes including a rotation - leaves getLogs empty and deletes both files`() =
        runTest {
            val logger = ServiceLogger(context, ServiceLogger.VpnServiceLoggerTag.WireGuard)
            val bigMessage = "x".repeat(1024)
            repeat(1100) { logger.debug(bigMessage) } // forces a rotation, so a backup file also exists

            logger.clearLogs()

            assertEquals(emptyList<String>(), logger.getLogs())
            val logDir = File(filesDir, "logs")
            assertTrue(File(logDir, "vpn_debug.log").exists().not())
            assertTrue(File(logDir, "vpn_debug.log.old").exists().not())
        }
}