package com.kape.login.domain.usecase

import com.kape.login.FakeAuthentication
import com.kape.login.FakeDebugLogs
import com.kape.login.FakeLocalSession
import com.kape.login.FakeVpn
import com.kape.login.Recorder
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

internal class LogoutUseCaseImplTest {
    private val recorder = Recorder()
    private val authentication = FakeAuthentication(recorder)
    private val localSession = FakeLocalSession(recorder)
    private val vpn = FakeVpn(recorder)
    private val logout = LogoutUseCaseImpl(authentication, localSession, vpn, FakeDebugLogs(recorder))

    @Test
    fun `clears local data and logs out on the server`() =
        runTest {
            logout.logout()

            assertEquals(listOf("clear local data", "clear logs", "server logout"), recorder.events)
        }

    @Test
    fun `disconnects a connected VPN first`() =
        runTest {
            vpn.connected = true

            logout.logout()

            assertEquals(
                listOf("schedule log clear", "disconnect", "clear local data", "clear logs", "server logout"),
                recorder.events,
            )
        }

    @Test
    fun `stops automation from reconnecting`() =
        runTest {
            localSession.automationEnabled = true

            logout.logout()

            assertEquals("mark disconnected by user", recorder.events.first())
        }

    @Test
    fun `clears local data even when the server can't be reached`() =
        runTest {
            authentication.logoutConfirmed = false

            assertFalse(logout.logout())
            assertEquals(true, "clear local data" in recorder.events)
        }
}