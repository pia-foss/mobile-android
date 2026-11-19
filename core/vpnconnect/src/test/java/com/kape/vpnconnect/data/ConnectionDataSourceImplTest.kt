package com.kape.vpnconnect.data

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.kape.data.WorkerTags
import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.settings.data.VpnProtocols
import com.kape.vpnconnect.platformsdk.ServiceLogger
import com.kape.vpnconnect.worker.PortForwardingWorker
import com.privateinternetaccess.account.AndroidAccountAPI
import io.mockk.coEvery
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.util.concurrent.TimeUnit

internal class ConnectionDataSourceImplTest {
    @TempDir
    lateinit var filesDir: File

    private val context: Context = mockk()
    private val accountApi: AndroidAccountAPI = mockk()
    private val connectionPrefs: ConnectionPrefs = mockk(relaxed = true)
    private val workManager: WorkManager = mockk(relaxed = true)
    private val settingsPrefs: SettingsPrefs = mockk()

    @BeforeEach
    fun setUp() {
        every { context.filesDir } returns filesDir
        coEvery { settingsPrefs.getSelectedProtocolNow() } returns VpnProtocols.WireGuard
    }

    private fun createDataSource(scope: CoroutineScope) =
        ConnectionDataSourceImpl(context, accountApi, connectionPrefs, workManager, settingsPrefs, scope)

    @Test
    fun `getVpnToken - no token available - returns an empty string`() =
        runTest {
            every { accountApi.vpnToken() } returns null

            assertEquals("", createDataSource(backgroundScope).getVpnToken())
        }

    @Test
    fun `getVpnToken - token available - returns it`() =
        runTest {
            every { accountApi.vpnToken() } returns "vpn_token_abc"

            assertEquals("vpn_token_abc", createDataSource(backgroundScope).getVpnToken())
        }

    @Test
    fun `startPortForwarding - enqueues a unique 15-minute worker that keeps an existing one`() =
        runTest {
            val request = slot<PeriodicWorkRequest>()

            createDataSource(backgroundScope).startPortForwarding()

            verify {
                workManager.enqueueUniquePeriodicWork(
                    WorkerTags.PORT_FORWARDING_WORKER,
                    ExistingPeriodicWorkPolicy.KEEP,
                    capture(request),
                )
            }
            assertEquals(PortForwardingWorker::class.java.name, request.captured.workSpec.workerClassName)
            assertEquals(TimeUnit.MINUTES.toMillis(15), request.captured.workSpec.intervalDuration)
        }

    @Test
    fun `stopPortForwarding - clears the gateway and binding info, then cancels the worker`() =
        runTest {
            createDataSource(this).stopPortForwarding()
            testScheduler.advanceUntilIdle()

            coVerifyOrder {
                connectionPrefs.clearGateway()
                connectionPrefs.clearPortBindingInfo()
                workManager.cancelUniqueWork(WorkerTags.PORT_FORWARDING_WORKER)
            }
        }

    @Test
    fun `getDebugLogs - returns the lines in the shared service log store`() =
        runTest {
            coEvery { settingsPrefs.getSelectedProtocolNow() } returns VpnProtocols.OpenVPN
            ServiceLogger(context, ServiceLogger.VpnServiceLoggerTag.WireGuard).info("tunnel up")

            val logs = createDataSource(backgroundScope).getDebugLogs()

            assertEquals(1, logs.size)
            assertTrue(logs.single().contains("I/WireGuard: tunnel up"))
        }

    @Test
    fun `clearDebugLogs - empties the shared service log store`() =
        runTest {
            coEvery { settingsPrefs.getSelectedProtocolNow() } returns VpnProtocols.Automatic
            ServiceLogger(context, ServiceLogger.VpnServiceLoggerTag.OpenVpn).info("tunnel up")
            val dataSource = createDataSource(backgroundScope)

            dataSource.clearDebugLogs()

            assertEquals(emptyList<String>(), dataSource.getDebugLogs())
        }
}