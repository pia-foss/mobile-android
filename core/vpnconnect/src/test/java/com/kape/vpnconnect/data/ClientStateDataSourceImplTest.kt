package com.kape.vpnconnect.data

import com.kape.data.NO_IP
import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.vpnconnect.utils.SHORT_DELAY
import com.privateinternetaccess.account.AccountRequestError
import com.privateinternetaccess.account.AndroidAccountAPI
import com.privateinternetaccess.account.model.response.ClientStatusInformation
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ClientStateDataSourceImplTest {
    private val accountAPI = mockk<AndroidAccountAPI>()
    private val connectionPrefs = mockk<ConnectionPrefs>(relaxed = true)

    private lateinit var dataSource: ClientStateDataSourceImpl

    // region getPublicIp

    @Test
    fun `getPublicIp - disconnected - returns IP, sets client IP and clears VPN IP`() =
        runTest {
            dataSource =
                ClientStateDataSourceImpl(
                    accountAPI = accountAPI,
                    connectionPrefs = connectionPrefs,
                    ioScope = this,
                )
            val slot = slot<(ClientStatusInformation?, List<AccountRequestError>) -> Unit>()
            every { accountAPI.clientStatus(SHORT_DELAY, capture(slot)) } answers {
                slot.captured.invoke(ClientStatusInformation(connected = false, ip = "192.168.1.1"), emptyList())
            }

            val result = dataSource.getPublicIp()

            assertEquals("192.168.1.1", result)
            coVerify { connectionPrefs.setClientIp("192.168.1.1") }
            coVerify { connectionPrefs.setVpnIp(NO_IP) }
        }

    @Test
    fun `getPublicIp - connected - returns IP without setting client IP`() =
        runTest {
            dataSource =
                ClientStateDataSourceImpl(
                    accountAPI = accountAPI,
                    connectionPrefs = connectionPrefs,
                    ioScope = this,
                )
            val slot = slot<(ClientStatusInformation?, List<AccountRequestError>) -> Unit>()
            every { accountAPI.clientStatus(SHORT_DELAY, capture(slot)) } answers {
                slot.captured.invoke(ClientStatusInformation(connected = true, ip = "10.0.0.1"), emptyList())
            }

            val result = dataSource.getPublicIp()

            assertEquals("10.0.0.1", result)
            coVerify(exactly = 0) { connectionPrefs.setClientIp(any()) }
        }

    @Test
    fun `getPublicIp - null response - returns NO_IP`() =
        runTest {
            dataSource =
                ClientStateDataSourceImpl(
                    accountAPI = accountAPI,
                    connectionPrefs = connectionPrefs,
                    ioScope = this,
                )

            val slot = slot<(ClientStatusInformation?, List<AccountRequestError>) -> Unit>()
            every { accountAPI.clientStatus(SHORT_DELAY, capture(slot)) } answers {
                slot.captured.invoke(null, emptyList())
            }

            val result = dataSource.getPublicIp()

            assertEquals(NO_IP, result)
        }

    // endregion

    // region getVpnIp

    @Test
    fun `getVpnIp - connected on first attempt - returns VPN IP`() =
        runTest {
            dataSource =
                ClientStateDataSourceImpl(
                    accountAPI = accountAPI,
                    connectionPrefs = connectionPrefs,
                    ioScope = this,
                )
            val slot = slot<(ClientStatusInformation?, List<AccountRequestError>) -> Unit>()
            every { accountAPI.clientStatus(SHORT_DELAY, capture(slot)) } answers {
                slot.captured.invoke(ClientStatusInformation(connected = true, ip = "10.8.0.1"), emptyList())
            }

            val result = dataSource.getVpnIp()

            assertEquals("10.8.0.1", result)
            coVerify { connectionPrefs.setVpnIp("10.8.0.1") }
        }

    @Test
    fun `getVpnIp - never connected - returns NO_IP after retries`() =
        runTest {
            dataSource =
                ClientStateDataSourceImpl(
                    accountAPI = accountAPI,
                    connectionPrefs = connectionPrefs,
                    ioScope = this,
                )
            val slot = slot<(ClientStatusInformation?, List<AccountRequestError>) -> Unit>()
            every { accountAPI.clientStatus(SHORT_DELAY, capture(slot)) } answers {
                slot.captured.invoke(ClientStatusInformation(connected = false, ip = NO_IP), emptyList())
            }

            val result = dataSource.getVpnIp()

            assertEquals(NO_IP, result)
        }

    @Test
    fun `getVpnIp - null response - returns NO_IP`() =
        runTest {
            dataSource =
                ClientStateDataSourceImpl(
                    accountAPI = accountAPI,
                    connectionPrefs = connectionPrefs,
                    ioScope = this,
                )
            val slot = slot<(ClientStatusInformation?, List<AccountRequestError>) -> Unit>()
            every { accountAPI.clientStatus(SHORT_DELAY, capture(slot)) } answers {
                slot.captured.invoke(null, emptyList())
            }

            val result = dataSource.getVpnIp()

            assertEquals(NO_IP, result)
        }

    // endregion
}