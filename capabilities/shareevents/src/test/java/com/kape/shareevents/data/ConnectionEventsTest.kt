package com.kape.shareevents.data

import com.kape.data.kpi.KpiConnectionEvent
import com.kape.data.kpi.KpiConnectionStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

internal class ConnectionEventsTest {
    @Test
    fun `connecting and reconnecting are attempts`() {
        assertEquals(
            KpiConnectionEvent.ConnectionAttempt,
            connectionEventFor(KpiConnectionStatus.Connecting, KpiConnectionStatus.NotConnected),
        )
        assertEquals(
            KpiConnectionEvent.ConnectionAttempt,
            connectionEventFor(KpiConnectionStatus.Reconnecting, KpiConnectionStatus.Connected),
        )
    }

    @Test
    fun `connected is established`() {
        assertEquals(
            KpiConnectionEvent.ConnectionEstablished,
            connectionEventFor(KpiConnectionStatus.Connected, KpiConnectionStatus.Connecting),
        )
    }

    @Test
    fun `disconnecting is only an event when an attempt was given up`() {
        assertEquals(
            KpiConnectionEvent.ConnectionCancelled,
            connectionEventFor(KpiConnectionStatus.NotConnected, KpiConnectionStatus.Connecting),
        )
        assertNull(connectionEventFor(KpiConnectionStatus.NotConnected, KpiConnectionStatus.Connected))
    }

    @Test
    fun `pausing isn't an event`() {
        assertNull(connectionEventFor(KpiConnectionStatus.Paused, KpiConnectionStatus.Connected))
    }
}