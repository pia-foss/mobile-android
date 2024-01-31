package com.kape.vpnconnect.data

import app.cash.turbine.test
import com.kape.connection.ConnectionPrefs
import com.kape.connection.NO_IP
import com.kape.vpnconnect.domain.ClientStateDataSource
import com.privateinternetaccess.account.AccountRequestError
import com.privateinternetaccess.account.AndroidAccountAPI
import com.privateinternetaccess.account.model.response.ClientStatusInformation
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull

class ClientStateDataSourceImplTest {
    private val accountAPI: AndroidAccountAPI = mockk()
    private val connectionPrefs: ConnectionPrefs = mockk()
    private lateinit var clientStateDataSource: ClientStateDataSource

    @BeforeEach
    fun setUp() {
        clientStateDataSource = ClientStateDataSourceImpl(accountAPI, connectionPrefs)
    }

    @Test
    fun `getClientStatus - connected - vpn ip is set`() = runTest {
        val info = ClientStatusInformation(connected = true, ip = "100.100.100.100")
        every { connectionPrefs.setClientVpnIp(any()) } returns Unit
        coEvery { accountAPI.clientStatus(any()) } answers {
            lastArg<(ClientStatusInformation?, List<AccountRequestError>) -> Unit>().invoke(
                info,
                emptyList(),
            )
        }
        clientStateDataSource.getClientStatus().test {
            val actual = awaitItem()
            assertNotNull(actual)
            verify { connectionPrefs.setClientVpnIp(info.ip) }
        }
    }

    @Test
    fun `getClientStatus - not connected - ip is set`() = runTest {
        val info = ClientStatusInformation(connected = false, ip = "100.100.100.100")
        every { connectionPrefs.setClientIp(any()) } returns Unit
        every { connectionPrefs.setClientVpnIp(any()) } returns Unit
        coEvery { accountAPI.clientStatus(any()) } answers {
            lastArg<(ClientStatusInformation?, List<AccountRequestError>) -> Unit>().invoke(
                info,
                emptyList(),
            )
        }
        clientStateDataSource.getClientStatus().test {
            val actual = awaitItem()
            assertNotNull(actual)
            verify { connectionPrefs.setClientIp(info.ip) }
            verify { connectionPrefs.setClientVpnIp(NO_IP) }
        }
    }

    @Test
    fun `getClientStatus - fails - ip is set to empty`() = runTest {
        every { connectionPrefs.setClientIp(any()) } returns Unit
        every { connectionPrefs.setClientVpnIp(any()) } returns Unit
        coEvery { accountAPI.clientStatus(any()) } answers {
            lastArg<(ClientStatusInformation?, List<AccountRequestError>) -> Unit>().invoke(
                null,
                listOf(AccountRequestError(-1, null, 0)),
            )
        }
        clientStateDataSource.getClientStatus().test {
            val actual = awaitItem()
            assertNotNull(actual)
            verify { connectionPrefs.setClientVpnIp(NO_IP) }
        }
    }
}