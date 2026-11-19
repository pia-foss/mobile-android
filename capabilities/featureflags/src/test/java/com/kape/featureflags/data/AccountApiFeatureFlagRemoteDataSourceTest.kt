package com.kape.featureflags.data

import com.privateinternetaccess.account.AccountRequestError
import com.privateinternetaccess.account.AndroidAccountAPI
import com.privateinternetaccess.account.model.response.FeatureFlagsInformation
import io.mockk.every
import io.mockk.invoke
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

internal class AccountApiFeatureFlagRemoteDataSourceTest {
    private val api = mockk<AndroidAccountAPI>()
    private val dataSource = AccountApiFeatureFlagRemoteDataSource(api)

    private fun respond(
        details: FeatureFlagsInformation?,
        errors: List<AccountRequestError> = emptyList(),
    ) {
        every { api.featureFlags(captureLambda()) } answers {
            lambda<(FeatureFlagsInformation?, List<AccountRequestError>) -> Unit>().invoke(details, errors)
        }
    }

    @Test
    fun `returns the fetched flags`() =
        runTest {
            respond(FeatureFlagsInformation(listOf("a", "b"), emptyList()))

            assertEquals(listOf("a", "b"), dataSource.fetchFlags())
        }

    @Test
    fun `fails when the api reports errors`() =
        runTest {
            respond(FeatureFlagsInformation(listOf("a"), emptyList()), listOf(AccountRequestError(500, "boom")))

            assertNull(dataSource.fetchFlags())
        }

    @Test
    fun `treats a response without details as no flags`() =
        runTest {
            respond(null)

            assertEquals(emptyList<String>(), dataSource.fetchFlags())
        }
}