package com.kape.dedicatedip.data

import app.cash.turbine.test
import com.kape.dedicatedip.domain.DipDataSource
import com.kape.dip.DipPrefs
import com.kape.dip.data.DedicatedIpSignupPlans
import com.privateinternetaccess.account.model.response.AndroidAddonsSubscriptionsInformation
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DipSignupRepositoryTest {

    private val dipPrefs: DipPrefs = mockk(relaxed = true)
    private val dipDataSource: DipDataSource = mockk(relaxed = true)

    private lateinit var dipSignupRepository: DipSignupRepository

    @BeforeEach
    internal fun setUp() {
        dipSignupRepository = DipSignupRepository(
            dipDataSource = dipDataSource,
            dipPrefs = dipPrefs,
        )
    }

    @Test
    fun `If there are no persisted signup plans - Fetch it`() = runTest {
        // given
        val fetchedDedicatedIpSignupPlansMock = AndroidAddonsSubscriptionsInformation(
            availableProducts = emptyList(),
            status = "fetched",
        )
        every { dipPrefs.getDedicatedIpSignupPlans() } returns null
        every { dipDataSource.signupPlans() } returns flow {
            emit(fetchedDedicatedIpSignupPlansMock)
        }

        // when
        dipSignupRepository.signupPlans().test {
            val actual = awaitItem()

            // then
            Assertions.assertEquals(fetchedDedicatedIpSignupPlansMock, actual)
        }
    }

    @Test
    fun `If there are persisted signup plans only 12 hours old - Return it`() = runTest {
        // given
        val fetchedDedicatedIpSignupPlansMock = AndroidAddonsSubscriptionsInformation(
            availableProducts = emptyList(),
            status = "0.5days",
        )
        val dedicatedIpSignupPlans = DedicatedIpSignupPlans(
            persistedTimestamp = System.currentTimeMillis() - (1L * 12 * 60 * 60 * 1000),
            signupPlans = fetchedDedicatedIpSignupPlansMock,
        )
        every { dipPrefs.getDedicatedIpSignupPlans() } returns dedicatedIpSignupPlans

        // when
        dipSignupRepository.signupPlans().test {
            val actual = awaitItem()

            // then
            Assertions.assertEquals(fetchedDedicatedIpSignupPlansMock, actual)
        }
    }

    @Test
    fun `If there are persisted signup plans 2 days old - Fetch it`() = runTest {
        // given
        val outdatedFetchedDedicatedIpSignupPlansMock = AndroidAddonsSubscriptionsInformation(
            availableProducts = emptyList(),
            status = "2days",
        )
        val updatedFetchedDedicatedIpSignupPlansMock = AndroidAddonsSubscriptionsInformation(
            availableProducts = emptyList(),
            status = "fetched",
        )
        val dedicatedIpSignupPlans = DedicatedIpSignupPlans(
            persistedTimestamp = System.currentTimeMillis() - (2L * 24 * 60 * 60 * 1000),
            signupPlans = outdatedFetchedDedicatedIpSignupPlansMock,
        )
        every { dipPrefs.getDedicatedIpSignupPlans() } returns dedicatedIpSignupPlans
        every { dipDataSource.signupPlans() } returns flow {
            emit(updatedFetchedDedicatedIpSignupPlansMock)
        }

        // when
        dipSignupRepository.signupPlans().test {
            val actual = awaitItem()

            // then
            Assertions.assertEquals(updatedFetchedDedicatedIpSignupPlansMock, actual)
        }
    }
}