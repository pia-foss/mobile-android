package com.kape.vpnconnect.domain

import com.kape.contracts.ConnectionStatusProvider
import com.kape.data.ConnectionStatus
import com.kape.localprefs.prefs.AutoProtocolNudgePrefs
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.platformsdk.vpn.service.models.KapeVpnTunnelError
import com.kape.settings.data.VpnProtocols
import com.kape.utils.NetworkConnectionListener
import com.kape.vpnconnect.data.AutoProtocolNudgeState
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.days

@OptIn(ExperimentalCoroutinesApi::class)
class ConnectionProblemDetectorTest {
    private val connectionStatusProvider = mockk<ConnectionStatusProvider>(relaxed = true)
    private val settingsPrefs = mockk<SettingsPrefs>(relaxed = true)
    private val clientStateDataSource = mockk<ClientStateDataSource>(relaxed = true)
    private val networkConnectionListener = mockk<NetworkConnectionListener>(relaxed = true)
    private val nudgePrefs = mockk<AutoProtocolNudgePrefs>(relaxed = true)

    private val statusFlow = MutableStateFlow<ConnectionStatus>(ConnectionStatus.DISCONNECTED)
    private val tunnelErrorFlow = MutableStateFlow<KapeVpnTunnelError?>(null)
    private val isConnectedFlow = MutableStateFlow(true)
    private val nudgeStateFlow = MutableStateFlow(AutoProtocolNudgeState())

    // Anchors the detector's virtual "now" to a real epoch so it stays comparable to the
    // real-wall-clock timestamps other tests bake into nudgeStateFlow (e.g. "8 days ago"),
    // while still advancing deterministically off the test scheduler via `currentTime`.
    private val baseMillis = System.currentTimeMillis()

    @BeforeEach
    fun setUp() {
        every { connectionStatusProvider.status } returns statusFlow
        every { connectionStatusProvider.lastTunnelError } returns tunnelErrorFlow
        every { networkConnectionListener.isConnected } returns isConnectedFlow
        every { nudgePrefs.state } returns nudgeStateFlow
        coEvery { nudgePrefs.getStateNow() } answers { nudgeStateFlow.value }
        coEvery { nudgePrefs.setState(any()) } answers { nudgeStateFlow.value = firstArg() }
        coEvery { settingsPrefs.getSelectedProtocolNow() } returns VpnProtocols.WireGuard
        coEvery { clientStateDataSource.isVpnTunnelReachable() } returns true
    }

    private fun createDetector(
        scope: CoroutineScope,
        now: () -> Long,
    ) = ConnectionProblemDetector(
        connectionStatusProvider,
        settingsPrefs,
        clientStateDataSource,
        networkConnectionListener,
        nudgePrefs,
        scope,
        now,
    )

    // region qualifying failures

    @Test
    fun `connect timeout - two attempts stuck past 30s within the short window - triggers the nudge`() =
        runTest(UnconfinedTestDispatcher()) {
            val detector = createDetector(backgroundScope, now = { baseMillis + currentTime })
            detector.start()

            statusFlow.value = ConnectionStatus.CONNECTING
            advanceTimeBy(31_000)
            assertFalse(detector.showNudge.value)

            statusFlow.value = ConnectionStatus.DISCONNECTED
            statusFlow.value = ConnectionStatus.CONNECTING
            advanceTimeBy(31_000)

            assertTrue(detector.showNudge.value)
            assertEquals(2, nudgeStateFlow.value.failureTimestamps.size)
        }

    @Test
    fun `immediate drop - disconnects within 15s of connecting - counts as a qualifying failure`() =
        runTest(UnconfinedTestDispatcher()) {
            val detector = createDetector(backgroundScope, now = { baseMillis + currentTime })
            detector.start()

            statusFlow.value = ConnectionStatus.CONNECTED
            advanceTimeBy(5_000)
            statusFlow.value = ConnectionStatus.DISCONNECTED

            assertEquals(1, nudgeStateFlow.value.failureTimestamps.size)
        }

    @Test
    fun `stays connected past 15s then disconnects - not counted as a failure`() =
        runTest(UnconfinedTestDispatcher()) {
            val detector = createDetector(backgroundScope, now = { baseMillis + currentTime })
            detector.start()

            statusFlow.value = ConnectionStatus.CONNECTED
            advanceTimeBy(20_000)
            statusFlow.value = ConnectionStatus.DISCONNECTED

            assertTrue(nudgeStateFlow.value.failureTimestamps.isEmpty())
        }

    @Test
    fun `dead tunnel - reachability check fails continuously past 60s while connected - counts as a failure`() =
        runTest(UnconfinedTestDispatcher()) {
            coEvery { clientStateDataSource.isVpnTunnelReachable() } returns false
            val detector = createDetector(backgroundScope, now = { baseMillis + currentTime })
            detector.start()

            statusFlow.value = ConnectionStatus.CONNECTED
            advanceTimeBy(76_000)

            assertEquals(1, nudgeStateFlow.value.failureTimestamps.size)
        }

    @Test
    fun `dead tunnel - reachability recovers before 60s - does not count as a failure`() =
        runTest(UnconfinedTestDispatcher()) {
            var reachable = true
            coEvery { clientStateDataSource.isVpnTunnelReachable() } answers { reachable }
            val detector = createDetector(backgroundScope, now = { baseMillis + currentTime })
            detector.start()

            statusFlow.value = ConnectionStatus.CONNECTED
            advanceTimeBy(15_000)
            reachable = false
            advanceTimeBy(30_000)
            reachable = true
            advanceTimeBy(76_000)

            assertTrue(nudgeStateFlow.value.failureTimestamps.isEmpty())
        }

    @Test
    fun `protocol not available pre-check - counts as a qualifying failure`() =
        runTest(UnconfinedTestDispatcher()) {
            val detector = createDetector(backgroundScope, now = { baseMillis + currentTime })
            detector.start()

            detector.onProtocolNotAvailable()

            assertEquals(1, nudgeStateFlow.value.failureTimestamps.size)
        }

    // endregion

    // region exclusions

    @Test
    fun `already on Automatic - does not record failures`() =
        runTest(UnconfinedTestDispatcher()) {
            coEvery { settingsPrefs.getSelectedProtocolNow() } returns VpnProtocols.Automatic
            val detector = createDetector(backgroundScope, now = { baseMillis + currentTime })
            detector.start()

            statusFlow.value = ConnectionStatus.CONNECTED
            advanceTimeBy(5_000)
            statusFlow.value = ConnectionStatus.DISCONNECTED

            assertTrue(nudgeStateFlow.value.failureTimestamps.isEmpty())
        }

    @Test
    fun `a tunnel error is present - not blamed on the protocol`() =
        runTest(UnconfinedTestDispatcher()) {
            tunnelErrorFlow.value = KapeVpnTunnelError.VpnPermissionRevoked
            val detector = createDetector(backgroundScope, now = { baseMillis + currentTime })
            detector.start()

            statusFlow.value = ConnectionStatus.CONNECTED
            advanceTimeBy(5_000)
            statusFlow.value = ConnectionStatus.DISCONNECTED

            assertTrue(nudgeStateFlow.value.failureTimestamps.isEmpty())
        }

    @Test
    fun `user-initiated disconnect - immediate drop is not blamed on the protocol`() =
        runTest(UnconfinedTestDispatcher()) {
            val detector = createDetector(backgroundScope, now = { baseMillis + currentTime })
            detector.start()

            statusFlow.value = ConnectionStatus.CONNECTED
            advanceTimeBy(5_000)
            detector.onUserInitiatedDisconnect()
            statusFlow.value = ConnectionStatus.DISCONNECTED

            assertTrue(nudgeStateFlow.value.failureTimestamps.isEmpty())
        }

    @Test
    fun `user-initiated disconnect flag only suppresses the very next disconnect`() =
        runTest(UnconfinedTestDispatcher()) {
            val detector = createDetector(backgroundScope, now = { baseMillis + currentTime })
            detector.start()

            statusFlow.value = ConnectionStatus.CONNECTED
            advanceTimeBy(5_000)
            detector.onUserInitiatedDisconnect()
            statusFlow.value = ConnectionStatus.DISCONNECTED

            statusFlow.value = ConnectionStatus.CONNECTED
            advanceTimeBy(5_000)
            statusFlow.value = ConnectionStatus.DISCONNECTED

            assertEquals(1, nudgeStateFlow.value.failureTimestamps.size)
        }

    @Test
    fun `device is offline - not blamed on the protocol`() =
        runTest(UnconfinedTestDispatcher()) {
            isConnectedFlow.value = false
            val detector = createDetector(backgroundScope, now = { baseMillis + currentTime })
            detector.start()

            statusFlow.value = ConnectionStatus.CONNECTED
            advanceTimeBy(5_000)
            statusFlow.value = ConnectionStatus.DISCONNECTED

            assertTrue(nudgeStateFlow.value.failureTimestamps.isEmpty())
        }

    // endregion

    // region frequency capping

    @Test
    fun `already prompted within 14 days - does not prompt again`() =
        runTest(UnconfinedTestDispatcher()) {
            nudgeStateFlow.value = AutoProtocolNudgeState(promptTimestamps = listOf(System.currentTimeMillis()))
            val detector = createDetector(backgroundScope, now = { baseMillis + currentTime })
            detector.start()

            statusFlow.value = ConnectionStatus.CONNECTED
            advanceTimeBy(5_000)
            statusFlow.value = ConnectionStatus.DISCONNECTED

            assertFalse(detector.showNudge.value)
            assertTrue(nudgeStateFlow.value.failureTimestamps.isEmpty())
        }

    @Test
    fun `dismissed recently - stays in the 7-day cooldown`() =
        runTest(UnconfinedTestDispatcher()) {
            nudgeStateFlow.value =
                AutoProtocolNudgeState(dismissCount = 1, lastDismissedAt = System.currentTimeMillis())
            val detector = createDetector(backgroundScope, now = { baseMillis + currentTime })
            detector.start()

            statusFlow.value = ConnectionStatus.CONNECTED
            advanceTimeBy(5_000)
            statusFlow.value = ConnectionStatus.DISCONNECTED

            assertTrue(nudgeStateFlow.value.failureTimestamps.isEmpty())
        }

    @Test
    fun `dismissed over 7 days ago - cooldown has expired`() =
        runTest(UnconfinedTestDispatcher()) {
            nudgeStateFlow.value =
                AutoProtocolNudgeState(
                    dismissCount = 1,
                    lastDismissedAt = System.currentTimeMillis() - 8.days.inWholeMilliseconds,
                )
            val detector = createDetector(backgroundScope, now = { baseMillis + currentTime })
            detector.start()

            statusFlow.value = ConnectionStatus.CONNECTED
            advanceTimeBy(5_000)
            statusFlow.value = ConnectionStatus.DISCONNECTED

            assertEquals(1, nudgeStateFlow.value.failureTimestamps.size)
        }

    @Test
    fun `dismissed twice already - permanently stopped`() =
        runTest(UnconfinedTestDispatcher()) {
            nudgeStateFlow.value =
                AutoProtocolNudgeState(
                    dismissCount = 2,
                    lastDismissedAt = System.currentTimeMillis() - 8.days.inWholeMilliseconds,
                )
            val detector = createDetector(backgroundScope, now = { baseMillis + currentTime })
            detector.start()

            statusFlow.value = ConnectionStatus.CONNECTED
            advanceTimeBy(5_000)
            statusFlow.value = ConnectionStatus.DISCONNECTED

            assertTrue(nudgeStateFlow.value.failureTimestamps.isEmpty())
        }

    @Test
    fun `already accepted the switch - never nudged again`() =
        runTest(UnconfinedTestDispatcher()) {
            nudgeStateFlow.value = AutoProtocolNudgeState(accepted = true)
            val detector = createDetector(backgroundScope, now = { baseMillis + currentTime })
            detector.start()

            statusFlow.value = ConnectionStatus.CONNECTED
            advanceTimeBy(5_000)
            statusFlow.value = ConnectionStatus.DISCONNECTED

            assertTrue(nudgeStateFlow.value.failureTimestamps.isEmpty())
        }

    // endregion

    // region user actions

    @Test
    fun `switching to Automatic is accepted - marks state accepted and hides the dialog`() =
        runTest(UnconfinedTestDispatcher()) {
            val detector = createDetector(backgroundScope, now = { baseMillis + currentTime })
            detector.start()

            detector.onSwitchToAutomaticAccepted()

            assertTrue(nudgeStateFlow.value.accepted)
            assertFalse(detector.showNudge.value)
        }

    @Test
    fun `dismissing the nudge - records the dismissal and hides the dialog`() =
        runTest(UnconfinedTestDispatcher()) {
            val detector = createDetector(backgroundScope, now = { baseMillis + currentTime })
            detector.start()

            detector.onNudgeDismissed()

            assertEquals(1, nudgeStateFlow.value.dismissCount)
            assertNotNull(nudgeStateFlow.value.lastDismissedAt)
            assertFalse(detector.showNudge.value)
        }

    // endregion
}