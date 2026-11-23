package com.kape.shareevents.data

import android.os.SystemClock
import com.kape.data.ConnectionStatus
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.settings.data.VpnProtocols
import com.privateinternetaccess.kpi.KPIAPI
import com.privateinternetaccess.kpi.KPIClientEvent
import com.privateinternetaccess.kpi.KPIError
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class KpiUsageEventsTest {
    private val api = FakeKpiApi()
    private val settingsPrefs =
        mockk<SettingsPrefs> { coEvery { getSelectedProtocolNow() } returns VpnProtocols.OpenVPN }
    private val events = KpiUsageEvents(api, KpiEventGenerator(testAppInfo, "pia-agent", settingsPrefs))

    @BeforeEach
    fun setUp() {
        mockkStatic(SystemClock::class)
        every { SystemClock.elapsedRealtime() } returns 0L
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(SystemClock::class)
    }

    @Test
    fun `submits an event for each connection step`() =
        runTest {
            events.connectionStatusChanged(ConnectionStatus.CONNECTING, isManual = true)
            events.connectionStatusChanged(ConnectionStatus.CONNECTED, isManual = true)

            assertEquals(listOf("VPN_CONNECTION_ATTEMPT", "VPN_CONNECTION_ESTABLISHED"), api.submitted.map { it.eventName })
        }

    @Test
    fun `a repeated status isn't submitted again`() =
        runTest {
            events.connectionStatusChanged(ConnectionStatus.CONNECTING, isManual = true)
            events.connectionStatusChanged(ConnectionStatus.CONNECTING, isManual = true)

            assertEquals(1, api.submitted.size)
        }

    @Test
    fun `the starting disconnected status isn't an event`() =
        runTest {
            events.connectionStatusChanged(ConnectionStatus.DISCONNECTED, isManual = true)

            assertTrue(api.submitted.isEmpty())
        }

    @Test
    fun `giving up a connection attempt is a cancellation`() =
        runTest {
            events.connectionStatusChanged(ConnectionStatus.CONNECTING, isManual = false)
            events.connectionStatusChanged(ConnectionStatus.DISCONNECTED, isManual = false)

            assertEquals("VPN_CONNECTION_CANCELLED", api.submitted.last().eventName)
        }

    @Test
    fun `cancelling through disconnecting is one cancellation`() =
        runTest {
            events.connectionStatusChanged(ConnectionStatus.CONNECTING, isManual = false)
            events.connectionStatusChanged(ConnectionStatus.DISCONNECTING, isManual = false)
            events.connectionStatusChanged(ConnectionStatus.DISCONNECTED, isManual = false)

            assertEquals(listOf("VPN_CONNECTION_ATTEMPT", "VPN_CONNECTION_CANCELLED"), api.submitted.map { it.eventName })
        }

    @Test
    fun `statuses without an event still count as the previous one`() =
        runTest {
            events.connectionStatusChanged(ConnectionStatus.CONNECTING, isManual = false)
            events.connectionStatusChanged(ConnectionStatus.PAUSED, isManual = false)
            events.connectionStatusChanged(ConnectionStatus.DISCONNECTED, isManual = false)

            assertEquals(listOf("VPN_CONNECTION_ATTEMPT"), api.submitted.map { it.eventName })
        }

    @Test
    fun `the source follows whether the change was manual`() =
        runTest {
            events.connectionStatusChanged(ConnectionStatus.CONNECTING, isManual = false)
            events.connectionStatusChanged(ConnectionStatus.CONNECTED, isManual = true)

            assertEquals(listOf("Automatic", "Manual"), api.submitted.map { it.eventProperties["connection_source"] })
        }

    @Test
    fun `purchase steps are submitted`() =
        runTest {
            events.purchaseStarted()
            events.purchaseSucceeded()
            events.purchaseFailed("declined")
            events.signupRetried(2, "timeout")

            assertEquals(
                listOf("iap_processing_purchase", "iap_processing_success", "iap_processing_failure", "iap_processing_retry"),
                api.submitted.map { it.eventName },
            )
        }

    @Test
    fun `recent events come from the api`() =
        runTest {
            api.recent = listOf("a", "b")

            assertEquals(listOf("a", "b"), events.recentEvents())
        }

    @Test
    fun `start, stop and flush reach the api`() {
        events.start()
        events.flush()
        events.stop()

        assertEquals(listOf("start", "flush", "stop"), api.calls)
    }
}

private class FakeKpiApi : KPIAPI {
    val submitted = mutableListOf<KPIClientEvent>()
    val calls = mutableListOf<String>()
    var recent = emptyList<String>()

    override fun start() {
        calls += "start"
    }

    override fun stop(callback: (KPIError?) -> Unit) {
        calls += "stop"
    }

    override fun submit(
        event: KPIClientEvent,
        callback: (KPIError?) -> Unit,
    ) {
        submitted += event
    }

    override fun flush(callback: (KPIError?) -> Unit) {
        calls += "flush"
    }

    override fun recentEvents(callback: (List<String>) -> Unit) = callback(recent)
}