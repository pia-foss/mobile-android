package com.kape.vpnconnect.data

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import com.kape.data.WorkerTags
import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.settings.data.VpnProtocols
import com.kape.vpnconnect.platformsdk.ServiceLogger
import com.privateinternetaccess.account.AndroidAccountAPI
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkConstructor
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Covers only what's actually live in [ConnectionDataSourceImpl] today. startConnection/
 * stopConnection/updateConfigurationServers were removed with the account/vpnmanager API during
 * the platform-SDK migration (connecting now goes through PiaService via ConnectionManagerImpl —
 * see ConnectionManagerImplTest) and are commented out pending a replacement, so there's nothing
 * left to test for them here.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ConnectionDataSourceImplTest {
    private val accountApi = mockk<AndroidAccountAPI>()
    private val connectionPrefs = mockk<ConnectionPrefs>(relaxed = true)
    private val workManager = mockk<WorkManager>(relaxed = true)
    private val settingsPrefs = mockk<SettingsPrefs>(relaxed = true)

    private fun newDataSource(scope: CoroutineScope) =
        ConnectionDataSourceImpl(
            accountApi = accountApi,
            connectionPrefs = connectionPrefs,
            workManager = workManager,
            settingsPrefs = settingsPrefs,
            ioScope = scope,
        )

    // region getVpnToken

    @Test
    fun `getVpnToken - account API returns a token - returns that token`() =
        runTest {
            val dataSource = newDataSource(this)
            every { accountApi.vpnToken() } returns "vpn_token_123"

            assertEquals("vpn_token_123", dataSource.getVpnToken())
        }

    @Test
    fun `getVpnToken - account API returns null - returns empty string`() =
        runTest {
            val dataSource = newDataSource(this)
            every { accountApi.vpnToken() } returns null

            assertEquals("", dataSource.getVpnToken())
        }

    // endregion

    // region startPortForwarding

    @Test
    fun `startPortForwarding - enqueues unique periodic work with KEEP policy`() =
        runTest {
            val dataSource = newDataSource(this)

            dataSource.startPortForwarding()

            verify {
                workManager.enqueueUniquePeriodicWork(
                    WorkerTags.PORT_FORWARDING_WORKER,
                    ExistingPeriodicWorkPolicy.KEEP,
                    any(),
                )
            }
        }

    // endregion

    // region stopPortForwarding

    @Test
    fun `stopPortForwarding - clears gateway, port binding info, and cancels the worker`() =
        runTest {
            val dataSource = newDataSource(this)

            dataSource.stopPortForwarding()
            advanceUntilIdle() // the cleanup runs on ioScope, not inline

            coVerify { connectionPrefs.clearGateway() }
            coVerify { connectionPrefs.clearPortBindingInfo() }
            verify { workManager.cancelUniqueWork(WorkerTags.PORT_FORWARDING_WORKER) }
        }

    // endregion

    // region getDebugLogs

    @Test
    fun `getDebugLogs - delegates to a ServiceLogger tagged for the selected protocol`() =
        runTest {
            mockkConstructor(ServiceLogger::class)
            try {
                coEvery { settingsPrefs.getSelectedProtocolNow() } returns VpnProtocols.WireGuard
                coEvery { anyConstructed<ServiceLogger>().getLogs() } returns listOf("log1", "log2")

                val dataSource = newDataSource(this)

                val result = dataSource.getDebugLogs()

                assertEquals(listOf("log1", "log2"), result)
            } finally {
                unmockkConstructor(ServiceLogger::class)
            }
        }

    // endregion
}