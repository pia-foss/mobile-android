package com.kape.shareevents.data

import com.kape.data.ConnectionStatus
import com.kape.data.kpi.KpiConnectionEvent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

internal class ConnectionEventsTest {
    @Test
    fun `connecting and reconnecting are attempts`() {
        assertEquals(
            KpiConnectionEvent.ConnectionAttempt,
            connectionEventFor(ConnectionStatus.CONNECTING, ConnectionStatus.DISCONNECTED),
        )
        assertEquals(
            KpiConnectionEvent.ConnectionAttempt,
            connectionEventFor(ConnectionStatus.RECONNECTING, ConnectionStatus.CONNECTED),
        )
    }

    @Test
    fun `connected is established`() {
        assertEquals(
            KpiConnectionEvent.ConnectionEstablished,
            connectionEventFor(ConnectionStatus.CONNECTED, ConnectionStatus.CONNECTING),
        )
    }

    @Test
    fun `disconnecting is only an event when an attempt was given up`() {
        assertEquals(
            KpiConnectionEvent.ConnectionCancelled,
            connectionEventFor(ConnectionStatus.DISCONNECTED, ConnectionStatus.CONNECTING),
        )
        assertEquals(
            KpiConnectionEvent.ConnectionCancelled,
            connectionEventFor(ConnectionStatus.DISCONNECTING, ConnectionStatus.CONNECTING),
        )
        assertNull(connectionEventFor(ConnectionStatus.DISCONNECTED, ConnectionStatus.CONNECTED))
        assertNull(connectionEventFor(ConnectionStatus.DISCONNECTED, ConnectionStatus.DISCONNECTING))
    }

    @Test
    fun `pausing isn't an event`() {
        assertNull(connectionEventFor(ConnectionStatus.PAUSED, ConnectionStatus.CONNECTED))
    }
}