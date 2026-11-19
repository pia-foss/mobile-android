package com.kape.shareevents.data

import android.os.SystemClock
import com.kape.contracts.AppInfo
import com.kape.data.kpi.KpiConnectionEvent
import com.kape.data.kpi.KpiConnectionSource
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.settings.data.VpnProtocols
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal val testAppInfo =
    object : AppInfo {
        override val buildFlavor = "google"
        override val buildType = "release"
        override val versionName = "4.2.0"
        override val versionCode = 420
        override val isFreshInstall = false
    }

internal class KpiEventGeneratorTest {
    private var now = 0L
    private val settingsPrefs =
        mockk<SettingsPrefs> { coEvery { getSelectedProtocolNow() } returns VpnProtocols.WireGuard }
    private val generator = KpiEventGenerator(testAppInfo, "pia-agent", settingsPrefs)

    @BeforeEach
    fun setUp() {
        mockkStatic(SystemClock::class)
        every { SystemClock.elapsedRealtime() } answers { now }
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(SystemClock::class)
    }

    private val purchaseProperties =
        mapOf("platform" to "Android", "version" to "4.2.0", "origin" to "signup", "environment" to "production")

    @Test
    fun `purchase events carry the signup origin and app version`() {
        with(generator.getProcessingPurchaseEvent()) {
            assertEquals("iap_processing_purchase", eventName)
            assertEquals(purchaseProperties, eventProperties)
        }
        with(generator.getProcessingSuccess()) {
            assertEquals("iap_processing_success", eventName)
            assertEquals(purchaseProperties, eventProperties)
        }
    }

    @Test
    fun `purchase failures carry the error`() {
        with(generator.getProcessingFailure("declined")) {
            assertEquals("iap_processing_failure", eventName)
            assertEquals(purchaseProperties + ("error" to "declined"), eventProperties)
        }
    }

    @Test
    fun `retries carry the error and the attempt`() {
        with(generator.getProcessingRetry(3, "timeout")) {
            assertEquals("iap_processing_retry", eventName)
            assertEquals(purchaseProperties + mapOf("error" to "timeout", "retryCount" to "3"), eventProperties)
        }
    }

    @Test
    fun `connection events describe the source, user agent and protocol`() =
        runTest {
            coEvery { settingsPrefs.getSelectedProtocolNow() } returns VpnProtocols.Automatic

            val event = generator.getConnectionEvent(KpiConnectionEvent.ConnectionAttempt, KpiConnectionSource.Manual)

            assertEquals("VPN_CONNECTION_ATTEMPT", event.eventName)
            assertEquals(
                mapOf(
                    "platform" to "Android",
                    "version" to "4.2.0",
                    "connection_source" to "Manual",
                    "user_agent" to "pia-agent",
                    "vpn_protocol" to "Automatic",
                ),
                event.eventProperties,
            )
        }

    @Test
    fun `an established connection reports the seconds since the attempt`() =
        runTest {
            now = 10_000
            generator.getConnectionEvent(KpiConnectionEvent.ConnectionAttempt, KpiConnectionSource.Automatic)
            now = 12_500

            val event = generator.getConnectionEvent(KpiConnectionEvent.ConnectionEstablished, KpiConnectionSource.Automatic)

            assertEquals("VPN_CONNECTION_ESTABLISHED", event.eventName)
            assertEquals("2.5", event.eventProperties["time_to_connect"])
            assertEquals("Automatic", event.eventProperties["connection_source"])
            assertEquals("WireGuard", event.eventProperties["vpn_protocol"])
        }

    @Test
    fun `only an established connection reports a time to connect`() =
        runTest {
            val attempt = generator.getConnectionEvent(KpiConnectionEvent.ConnectionAttempt, KpiConnectionSource.Manual)
            val cancelled = generator.getConnectionEvent(KpiConnectionEvent.ConnectionCancelled, KpiConnectionSource.Manual)

            assertEquals("VPN_CONNECTION_CANCELLED", cancelled.eventName)
            assertFalse("time_to_connect" in attempt.eventProperties)
            assertFalse("time_to_connect" in cancelled.eventProperties)
        }
}