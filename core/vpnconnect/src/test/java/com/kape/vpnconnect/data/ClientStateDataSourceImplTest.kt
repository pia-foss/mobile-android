package com.kape.vpnconnect.data

import com.kape.data.NO_IP
import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.localprefs.prefs.CsiPrefs
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.vpnconnect.utils.STATUS_REQUEST_LONG_TIMEOUT
import com.privateinternetaccess.account.AccountRequestError
import com.privateinternetaccess.account.AndroidAccountAPI
import com.privateinternetaccess.account.model.response.ClientStatusInformation
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ClientStateDataSourceImplTest {
    private val accountAPI = mockk<AndroidAccountAPI>()
    private val connectionPrefs = mockk<ConnectionPrefs>(relaxed = true)
    private val csiPrefs = mockk<CsiPrefs>(relaxed = true)
    private val settingsPrefs = mockk<SettingsPrefs>(relaxed = true)

    private lateinit var dataSource: ClientStateDataSourceImpl

    @BeforeEach
    fun setUp() {
        dataSource =
            ClientStateDataSourceImpl(
                accountAPI = accountAPI,
                connectionPrefs = connectionPrefs,
                csiPrefs = csiPrefs,
                settingsPrefs = settingsPrefs,
            )
    }

    // region getPublicIp

    @Test
    fun `getPublicIp - disconnected - returns IP, sets client IP and clears VPN IP`() =
        runTest {
            val slot = slot<(ClientStatusInformation?, List<AccountRequestError>) -> Unit>()
            every { accountAPI.clientStatus(STATUS_REQUEST_LONG_TIMEOUT, capture(slot)) } answers {
                slot.captured.invoke(ClientStatusInformation(connected = false, ip = "192.168.1.1"), emptyList())
            }

            val result = dataSource.getPublicIp()

            assertEquals("192.168.1.1", result)
            verify { connectionPrefs.setClientIp("192.168.1.1") }
            verify { connectionPrefs.setVpnIp(NO_IP) }
        }

    @Test
    fun `getPublicIp - connected - returns IP without setting client IP`() =
        runTest {
            val slot = slot<(ClientStatusInformation?, List<AccountRequestError>) -> Unit>()
            every { accountAPI.clientStatus(STATUS_REQUEST_LONG_TIMEOUT, capture(slot)) } answers {
                slot.captured.invoke(ClientStatusInformation(connected = true, ip = "10.0.0.1"), emptyList())
            }

            val result = dataSource.getPublicIp()

            assertEquals("10.0.0.1", result)
            verify(exactly = 0) { connectionPrefs.setClientIp(any()) }
        }

    @Test
    fun `getPublicIp - null response - returns NO_IP`() =
        runTest {
            val slot = slot<(ClientStatusInformation?, List<AccountRequestError>) -> Unit>()
            every { accountAPI.clientStatus(STATUS_REQUEST_LONG_TIMEOUT, capture(slot)) } answers {
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
            val slot = slot<(ClientStatusInformation?, List<AccountRequestError>) -> Unit>()
            every { accountAPI.clientStatus(STATUS_REQUEST_LONG_TIMEOUT, capture(slot)) } answers {
                slot.captured.invoke(ClientStatusInformation(connected = true, ip = "10.8.0.1"), emptyList())
            }

            val result = dataSource.getVpnIp()

            assertEquals("10.8.0.1", result)
            verify { connectionPrefs.setVpnIp("10.8.0.1") }
        }

    @Test
    fun `getVpnIp - never connected - returns NO_IP after retries`() =
        runTest {
            val slot = slot<(ClientStatusInformation?, List<AccountRequestError>) -> Unit>()
            every { accountAPI.clientStatus(STATUS_REQUEST_LONG_TIMEOUT, capture(slot)) } answers {
                slot.captured.invoke(ClientStatusInformation(connected = false, ip = NO_IP), emptyList())
            }

            val result = dataSource.getVpnIp()

            assertEquals(NO_IP, result)
        }

    @Test
    fun `getVpnIp - null response - returns NO_IP`() =
        runTest {
            val slot = slot<(ClientStatusInformation?, List<AccountRequestError>) -> Unit>()
            every { accountAPI.clientStatus(STATUS_REQUEST_LONG_TIMEOUT, capture(slot)) } answers {
                slot.captured.invoke(null, emptyList())
            }

            val result = dataSource.getVpnIp()

            assertEquals(NO_IP, result)
        }

    // endregion
}