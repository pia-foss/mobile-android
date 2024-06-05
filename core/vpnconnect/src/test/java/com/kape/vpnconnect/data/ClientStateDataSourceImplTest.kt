package com.kape.vpnconnect.data

import app.cash.turbine.test
import com.kape.connection.ConnectionPrefs
import com.kape.connection.NO_IP
import com.kape.csi.CsiPrefs
import com.kape.settings.SettingsPrefs
import com.kape.vpnconnect.domain.ClientStateDataSource
import com.privateinternetaccess.account.AccountRequestError
import com.privateinternetaccess.account.AndroidAccountAPI
import com.privateinternetaccess.account.model.response.ClientStatusInformation
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull

class ClientStateDataSourceImplTest {
    private val accountAPI: AndroidAccountAPI = mockk()
    private val connectionPrefs: ConnectionPrefs = mockk()
    private val csiPrefs: CsiPrefs = mockk<CsiPrefs>().apply {
        every { addCustomDebugLogs(any(), any()) } returns Unit
    }
    private val settingsPrefs: SettingsPrefs =
        mockk<SettingsPrefs>().apply { every { isDebugLoggingEnabled() } returns false }
    private lateinit var clientStateDataSource: ClientStateDataSource

    @BeforeEach
    fun setUp() {
        clientStateDataSource =
            ClientStateDataSourceImpl(accountAPI, connectionPrefs, csiPrefs, settingsPrefs)
    }

    @Test
    fun `getClientStatus - connected - vpn ip is set`() = runTest {
        val info = ClientStatusInformation(connected = true, ip = "100.100.100.100")
        every { connectionPrefs.setVpnIp(any()) } returns Unit
        coEvery { accountAPI.clientStatus(any(), any()) } answers {
            lastArg<(ClientStatusInformation?, List<AccountRequestError>) -> Unit>().invoke(
                info,
                emptyList(),
            )
        }
        clientStateDataSource.getClientStatus().test {
            val actual = awaitItem()
            assertNotNull(actual)
            verify { connectionPrefs.setVpnIp(info.ip) }
        }
    }

    @Test
    fun `getClientStatus - not connected - ip is set`() = runTest {
        val info = ClientStatusInformation(connected = false, ip = "100.100.100.100")
        every { connectionPrefs.setClientIp(any()) } returns Unit
        every { connectionPrefs.setVpnIp(any()) } returns Unit
        coEvery { accountAPI.clientStatus(any(), any()) } answers {
            lastArg<(ClientStatusInformation?, List<AccountRequestError>) -> Unit>().invoke(
                info,
                emptyList(),
            )
        }
        clientStateDataSource.getClientStatus().test {
            val actual = awaitItem()
            assertNotNull(actual)
            verify { connectionPrefs.setClientIp(info.ip) }
            verify { connectionPrefs.setVpnIp(NO_IP) }
        }
    }

    @Test
    fun `getClientStatus - fails - ip is set to empty`() = runTest {
        every { connectionPrefs.setClientIp(any()) } returns Unit
        every { connectionPrefs.setVpnIp(any()) } returns Unit
        coEvery { accountAPI.clientStatus(any(), any()) } answers {
            lastArg<(ClientStatusInformation?, List<AccountRequestError>) -> Unit>().invoke(
                null,
                listOf(AccountRequestError(-1, null, 0)),
            )
        }
        clientStateDataSource.getClientStatus().test {
            val firstItem = awaitItem()
            val secondItem = awaitItem()
            assertNotNull(firstItem)
            assertNotNull(secondItem)
            verify { connectionPrefs.setVpnIp(NO_IP) }
        }
    }

    @Test
    fun `getClientStatus - fails the first time - succeeds the second time - ip is set`() =
        runTest {
            val secondCallClientStatus =
                ClientStatusInformation(connected = true, ip = "100.100.100.100")
            every { connectionPrefs.setClientIp(any()) } returns Unit
            every { connectionPrefs.setVpnIp(any()) } returns Unit
            var callCount = 0
            coEvery { accountAPI.clientStatus(any(), any()) } answers {
                when (++callCount) {
                    1 -> {
                        lastArg<(ClientStatusInformation?, List<AccountRequestError>) -> Unit>().invoke(
                            null,
                            listOf(AccountRequestError(600, "Timeout", 0)),
                        )
                    }

                    2 -> {
                        lastArg<(ClientStatusInformation?, List<AccountRequestError>) -> Unit>().invoke(
                            secondCallClientStatus,
                            emptyList(),
                        )
                    }

                    else -> throw IllegalStateException("Unexpected call to clientStatus")
                }
            }
            clientStateDataSource.getClientStatus().test {
                val firstItem = awaitItem()
                val secondItem = awaitItem()
                assert(!firstItem)
                assert(secondItem)

                verifyOrder {
                    connectionPrefs.setVpnIp(NO_IP)
                    connectionPrefs.setVpnIp(secondCallClientStatus.ip)
                }
            }
        }
}