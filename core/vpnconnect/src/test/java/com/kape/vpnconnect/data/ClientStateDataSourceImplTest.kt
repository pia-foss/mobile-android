package com.kape.vpnconnect.data

import com.kape.contracts.NetworkStatus
import com.kape.data.NO_IP
import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.vpnconnect.utils.DELAY_BETWEEN_RETRY
import com.privateinternetaccess.account.AccountRequestError
import com.privateinternetaccess.account.AndroidAccountAPI
import com.privateinternetaccess.account.model.response.ClientStatusInformation
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class ClientStateDataSourceImplTest {
    private val accountApi: AndroidAccountAPI = mockk()
    private val connectionPrefs: ConnectionPrefs = mockk(relaxed = true)
    private val networkStatus = FakeNetworkStatus()

    private val responses = ArrayDeque<ClientStatusInformation?>()
    private var requests = 0

    @BeforeEach
    fun setUp() {
        every { accountApi.clientStatus(any(), any()) } answers {
            requests++
            val response = if (responses.size > 1) responses.removeFirst() else responses.firstOrNull()
            secondArg<(ClientStatusInformation?, List<AccountRequestError>) -> Unit>()(response, emptyList())
        }
        coEvery { connectionPrefs.getClientIpNow() } returns CLIENT_IP
    }

    private fun createDataSource(scope: CoroutineScope) = ClientStateDataSourceImpl(accountApi, connectionPrefs, networkStatus, scope)

    @Test
    fun `getPublicIp - disconnected status - stores the client ip and clears the vpn ip`() =
        runTest {
            responses += ClientStatusInformation(connected = false, ip = CLIENT_IP)

            val ip = createDataSource(backgroundScope).getPublicIp()

            assertEquals(CLIENT_IP, ip)
            assertEquals(1, requests)
            coVerify { connectionPrefs.setClientIp(CLIENT_IP) }
            coVerify { connectionPrefs.setVpnIp(NO_IP) }
        }

    @Test
    fun `getPublicIp - status reports connected - returns the ip without storing it as the client ip`() =
        runTest {
            responses += ClientStatusInformation(connected = true, ip = VPN_IP)

            val ip = createDataSource(backgroundScope).getPublicIp()

            assertEquals(VPN_IP, ip)
            coVerify(exactly = 0) { connectionPrefs.setClientIp(any()) }
            coVerify(exactly = 0) { connectionPrefs.setVpnIp(any()) }
        }

    @Test
    fun `getPublicIp - request fails then succeeds - retries after a delay`() =
        runTest {
            responses += null
            responses += ClientStatusInformation(connected = false, ip = CLIENT_IP)

            val ip = createDataSource(backgroundScope).getPublicIp()

            assertEquals(CLIENT_IP, ip)
            assertEquals(2, requests)
            assertEquals(DELAY_BETWEEN_RETRY, currentTime)
        }

    @Test
    fun `getPublicIp - every request fails - gives up after three attempts`() =
        runTest {
            responses += null

            val ip = createDataSource(backgroundScope).getPublicIp()

            assertEquals(NO_IP, ip)
            assertEquals(3, requests)
            coVerify(exactly = 0) { connectionPrefs.setClientIp(any()) }
        }

    @Test
    fun `getPublicIp - request fails while offline - stops without retrying`() =
        runTest {
            networkStatus.isOnline.value = false
            responses += null

            val ip = createDataSource(backgroundScope).getPublicIp()

            assertEquals(NO_IP, ip)
            assertEquals(1, requests)
            assertEquals(0L, currentTime)
        }

    @Test
    fun `getVpnIp - connected with an ip different from the client ip - stores and returns it`() =
        runTest {
            responses += ClientStatusInformation(connected = true, ip = VPN_IP)

            val ip = createDataSource(backgroundScope).getVpnIp()

            assertEquals(VPN_IP, ip)
            assertEquals(1, requests)
            coVerify { connectionPrefs.setVpnIp(VPN_IP) }
            coVerify(exactly = 0) { connectionPrefs.setClientIp(any()) }
        }

    @Test
    fun `getVpnIp - ip still matches the client ip - retries until the tunnel ip shows up`() =
        runTest {
            responses += ClientStatusInformation(connected = false, ip = CLIENT_IP)
            responses += ClientStatusInformation(connected = false, ip = CLIENT_IP)
            responses += ClientStatusInformation(connected = true, ip = VPN_IP)

            val ip = createDataSource(backgroundScope).getVpnIp()

            assertEquals(VPN_IP, ip)
            assertEquals(3, requests)
            assertEquals(2 * DELAY_BETWEEN_RETRY, currentTime)
        }

    @Test
    fun `getVpnIp - ip never changes from the client ip - gives up after five attempts`() =
        runTest {
            responses += ClientStatusInformation(connected = false, ip = CLIENT_IP)

            val ip = createDataSource(backgroundScope).getVpnIp()

            assertEquals(NO_IP, ip)
            assertEquals(5, requests)
        }

    @Test
    fun `getVpnIp - request fails while offline - stops without retrying`() =
        runTest {
            networkStatus.isOnline.value = false
            responses += null

            val ip = createDataSource(backgroundScope).getVpnIp()

            assertEquals(NO_IP, ip)
            verify(exactly = 1) { accountApi.clientStatus(any(), any()) }
        }

    private class FakeNetworkStatus : NetworkStatus {
        override val isOnline = MutableStateFlow(true)
    }

    private companion object {
        const val CLIENT_IP = "1.2.3.4"
        const val VPN_IP = "10.0.0.1"
    }
}