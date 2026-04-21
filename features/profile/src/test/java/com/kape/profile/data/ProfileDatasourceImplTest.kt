package com.kape.profile.data

import com.kape.profile.domain.ProfileDatasource
import com.privateinternetaccess.account.AccountRequestError
import com.privateinternetaccess.account.AndroidAccountAPI
import com.privateinternetaccess.account.model.response.AccountInformation
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class ProfileDatasourceImplTest {
    private val api: AndroidAccountAPI = mockk(relaxed = true)
    private lateinit var dataSource: ProfileDatasource

    @BeforeEach
    internal fun setUp() {
        dataSource = ProfileDatasourceImpl(api)
    }

    @Test
    fun `accountDetails - success`() =
        runTest {
            val accountInformation =
                AccountInformation(
                    active = true,
                    canInvite = false,
                    canceled = false,
                    daysRemaining = 1,
                    email = "email",
                    expirationTime = 0,
                    expireAlert = false,
                    needsPayment = false,
                    plan = "plan",
                    recurring = false,
                    renewUrl = "url",
                    renewable = false,
                    expired = false,
                    username = "username",
                    productId = null,
                )
            coEvery { api.accountDetails(any()) } answers {
                lastArg<(AccountInformation?, List<AccountRequestError>) -> Unit>().invoke(
                    accountInformation,
                    emptyList(),
                )
            }
            val actual = dataSource.accountDetails()
            assertNotNull(actual)
            assertEquals(accountInformation.username, actual.username)
        }

    @Test
    fun `accountDetails - failure`() =
        runTest {
            coEvery { api.accountDetails(any()) } answers {
                lastArg<(AccountInformation?, List<AccountRequestError>) -> Unit>().invoke(
                    null,
                    emptyList(),
                )
            }
            val actual = dataSource.accountDetails()
            assertNull(actual)
        }

    @Test
    fun `deleteAccount - success`() =
        runTest {
            coEvery { api.deleteAccount(any()) } answers {
                lastArg<(List<AccountRequestError>) -> Unit>().invoke(emptyList())
            }
            val actual = dataSource.deleteAccount()
            assertTrue(actual)
        }

    @Test
    fun `deleteAccount - failure`() =
        runTest {
            coEvery { api.deleteAccount(any()) } answers {
                lastArg<(List<AccountRequestError>) -> Unit>().invoke(listOf(mockk()))
            }
            val actual = dataSource.deleteAccount()
            assertFalse(actual)
        }
}