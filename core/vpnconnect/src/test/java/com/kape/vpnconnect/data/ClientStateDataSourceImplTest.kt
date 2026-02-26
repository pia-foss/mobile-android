package com.kape.vpnconnect.data

import com.kape.connection.ConnectionPrefs
import com.kape.connection.NO_IP
import com.kape.csi.CsiPrefs
import com.kape.settings.SettingsPrefs
import com.kape.vpnconnect.utils.ConnectionStatus
import com.kape.vpnconnect.utils.STATUS_REQUEST_LONG_TIMEOUT
import com.privateinternetaccess.account.AccountRequestError
import com.privateinternetaccess.account.AndroidAccountAPI
import com.privateinternetaccess.account.model.response.ClientStatusInformation
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ClientStateDataSourceImplTest {

    private val accountAPI = mockk<AndroidAccountAPI>()
    private val connectionPrefs = mockk<ConnectionPrefs>(relaxed = true)
    private val csiPrefs = mockk<CsiPrefs>(relaxed = true)
    private val settingsPrefs = mockk<SettingsPrefs>()

    private lateinit var dataSource: ClientStateDataSourceImpl

    private val dispatcher = StandardTestDispatcher()
    private fun connectedStatus(ip: String = "10.0.0.1") =
        ClientStatusInformation(connected = true, ip = ip)

    private fun disconnectedStatus(ip: String = "192.168.1.1") =
        ClientStatusInformation(connected = false, ip = ip)

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(dispatcher)

        every { settingsPrefs.isDebugLoggingEnabled() } returns true

        dataSource = ClientStateDataSourceImpl(
            accountAPI = accountAPI,
            connectionPrefs = connectionPrefs,
            csiPrefs = csiPrefs,
            settingsPrefs = settingsPrefs,
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `CONNECTED emits true and sets VPN IP`() = runTest {
        val slot = slot<(ClientStatusInformation?, List<AccountRequestError>) -> Unit>()

        every { accountAPI.clientStatus(callback = capture(slot)) } answers {
            slot.captured.invoke(connectedStatus(), emptyList())
        }

        val result = dataSource
            .getClientStatus(ConnectionStatus.CONNECTED)
            .first()

        assertTrue(result)

        verify { connectionPrefs.setVpnIp("10.0.0.1") }
    }

    @Test
    fun `DISCONNECTED emits true and sets client IP`() = runTest {
        val slot = slot<(ClientStatusInformation?, List<AccountRequestError>) -> Unit>()

        every { accountAPI.clientStatus(callback = capture(slot)) } answers {
            slot.captured.invoke(disconnectedStatus(), emptyList())
        }

        val result = dataSource
            .getClientStatus(ConnectionStatus.DISCONNECTED)
            .first()

        assertTrue(result)

        verify {
            connectionPrefs.setClientIp("192.168.1.1")
            connectionPrefs.setVpnIp(NO_IP)
        }
    }

    @Test
    fun `retries when status does not match`() = runTest {
        val firstSlot = slot<(ClientStatusInformation?, List<AccountRequestError>) -> Unit>()
        val retrySlot = slot<(ClientStatusInformation?, List<AccountRequestError>) -> Unit>()

        every { accountAPI.clientStatus(callback = capture(firstSlot)) } answers {
            firstSlot.captured.invoke(disconnectedStatus(), emptyList())
        }

        every {
            accountAPI.clientStatus(STATUS_REQUEST_LONG_TIMEOUT, capture(retrySlot))
        } answers {
            retrySlot.captured.invoke(connectedStatus(), emptyList())
        }

        val result = dataSource
            .getClientStatus(ConnectionStatus.CONNECTED)
            .first()

        assertTrue(result)

        verify { accountAPI.clientStatus(STATUS_REQUEST_LONG_TIMEOUT, any()) }
    }

    @Test
    fun `retries when error list is not empty`() = runTest {
        val firstSlot = slot<(ClientStatusInformation?, List<AccountRequestError>) -> Unit>()
        val retrySlot = slot<(ClientStatusInformation?, List<AccountRequestError>) -> Unit>()

        every { accountAPI.clientStatus(callback = capture(firstSlot)) } answers {
            firstSlot.captured.invoke(
                connectedStatus(),
                listOf(mockk())
            )
        }

        every {
            accountAPI.clientStatus(STATUS_REQUEST_LONG_TIMEOUT, capture(retrySlot))
        } answers {
            retrySlot.captured.invoke(connectedStatus(), emptyList())
        }

        val result = dataSource
            .getClientStatus(ConnectionStatus.CONNECTED)
            .first()

        assertTrue(result)
    }

    @Test
    fun `null status emits false`() = runTest {
        val firstSlot =
            slot<(ClientStatusInformation?, List<AccountRequestError>) -> Unit>()
        val retrySlot =
            slot<(ClientStatusInformation?, List<AccountRequestError>) -> Unit>()

        // FIRST call (default timeout = 1000)
        every { accountAPI.clientStatus(callback = capture(firstSlot)) } answers {
            firstSlot.captured.invoke(null, emptyList())
        }

        // RETRY call (long timeout = 3000)
        every {
            accountAPI.clientStatus(STATUS_REQUEST_LONG_TIMEOUT, capture(retrySlot))
        } answers {
            retrySlot.captured.invoke(null, emptyList())
        }

        val result =
            dataSource.getClientStatus(ConnectionStatus.CONNECTED).first()

        assertFalse(result)

        verify {
            connectionPrefs.setVpnIp(NO_IP)
        }
    }

}
