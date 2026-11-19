package com.kape.dedicatedip.data

import com.kape.dedicatedip.domain.DipActivationResult
import com.kape.localprefs.prefs.DipPrefs
import com.privateinternetaccess.account.AccountRequestError
import com.privateinternetaccess.account.AndroidAccountAPI
import com.privateinternetaccess.account.model.response.DedicatedIPInformationResponse
import com.privateinternetaccess.account.model.response.DedicatedIPInformationResponse.DedicatedIPInformation
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.invoke
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class AccountDedicatedIpRepositoryTest {
    private val dip =
        DedicatedIPInformation(
            "de",
            "203.0.113.7",
            "dip-cn",
            emptyList(),
            null,
            "dip-token",
            DedicatedIPInformationResponse.Status.active,
            false,
            emptyMap(),
        )

    // The cached flow is empty until something collects it; removal must read the stored list instead.
    private val dipPrefs =
        mockk<DipPrefs>(relaxed = true) {
            every { dedicatedIps } returns MutableStateFlow(emptyList())
            coEvery { getDedicatedIpsNow() } returns listOf(dip)
        }
    private val api = mockk<AndroidAccountAPI>()
    private val repository = AccountDedicatedIpRepository(api, dipPrefs)

    private fun respondToRedeem(
        details: List<DedicatedIPInformation>,
        errors: List<AccountRequestError> = emptyList(),
    ) {
        every { api.redeemDedicatedIPs(listOf("dip-token"), captureLambda()) } answers {
            lambda<(List<DedicatedIPInformation>, List<AccountRequestError>) -> Unit>().invoke(details, errors)
        }
    }

    @Test
    fun `stores an active dedicated IP`() =
        runTest {
            respondToRedeem(listOf(dip))

            assertEquals(DipActivationResult.Active, repository.activate("dip-token"))
            coVerify { dipPrefs.addDedicatedIp(dip) }
        }

    @Test
    fun `reports expired and invalid tokens without storing them`() =
        runTest {
            respondToRedeem(listOf(dip.copy(status = DedicatedIPInformationResponse.Status.expired)))
            assertEquals(DipActivationResult.Expired, repository.activate("dip-token"))

            respondToRedeem(listOf(dip.copy(status = DedicatedIPInformationResponse.Status.invalid)))
            assertEquals(DipActivationResult.Invalid, repository.activate("dip-token"))

            coVerify(exactly = 0) { dipPrefs.addDedicatedIp(any()) }
        }

    @Test
    fun `fails when the api reports errors or doesn't return the token`() =
        runTest {
            respondToRedeem(listOf(dip), listOf(AccountRequestError(500, "boom", 0)))
            assertEquals(DipActivationResult.Error, repository.activate("dip-token"))

            respondToRedeem(listOf(dip.copy(dipToken = "other-token")))
            assertEquals(DipActivationResult.Error, repository.activate("dip-token"))

            coVerify(exactly = 0) { dipPrefs.addDedicatedIp(any()) }
        }

    @Test
    fun `removes a stored dedicated IP even when nothing observes the list`() =
        runTest {
            assertTrue(repository.remove("dip-token"))
            coVerify { dipPrefs.removeDedicatedIp(dip) }
        }

    @Test
    fun `removing an unknown token changes nothing`() =
        runTest {
            assertFalse(repository.remove("other-token"))
            coVerify(exactly = 0) { dipPrefs.removeDedicatedIp(any()) }
        }
}