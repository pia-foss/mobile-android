package com.kape.vpnconnect.data

import android.content.Context
import com.kape.data.ConnectionStatus
import com.kape.platformsdk.vpn.service.models.KapeVPNConnectionStatus
import com.kape.vpnconnect.utils.NotificationHandler
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.TestScope
import org.junit.jupiter.api.Test

class VpnStatusTrackerTest {
    private val context: Context = mockk { every { getString(any()) } returns "" }
    private val notificationHandler: NotificationHandler = mockk(relaxed = true)
    private val tracker = VpnStatusTracker(context, notificationHandler, TestScope())

    @Test
    fun `disconnecting after a connection cancels the notification instead of updating it`() {
        tracker.onSdkStatusChanged(KapeVPNConnectionStatus.Connecting)
        tracker.onSdkStatusChanged(KapeVPNConnectionStatus.Disconnected)

        verify(exactly = 1) { notificationHandler.cancel() }
        verify(exactly = 0) { notificationHandler.update(ConnectionStatus.DISCONNECTED.toString()) }
    }

    @Test
    fun `non-disconnected status changes update the notification`() {
        tracker.onSdkStatusChanged(KapeVPNConnectionStatus.Connecting)
        tracker.onSdkStatusChanged(KapeVPNConnectionStatus.Reconnecting)

        verify { notificationHandler.update(ConnectionStatus.CONNECTING.toString()) }
        verify { notificationHandler.update(ConnectionStatus.RECONNECTING.toString()) }
        verify(exactly = 0) { notificationHandler.cancel() }
    }

    @Test
    fun `repeated disconnected status does not touch the notification`() {
        tracker.onSdkStatusChanged(KapeVPNConnectionStatus.Disconnected)

        verify(exactly = 0) { notificationHandler.cancel() }
        verify(exactly = 0) { notificationHandler.update(any()) }
    }
}