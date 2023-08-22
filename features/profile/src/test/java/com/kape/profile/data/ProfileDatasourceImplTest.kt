package com.kape.profile.data

import app.cash.turbine.test
import com.kape.profile.di.profileModule
import com.kape.profile.domain.ProfileDatasource
import com.privateinternetaccess.account.AccountRequestError
import com.privateinternetaccess.account.AndroidAccountAPI
import com.privateinternetaccess.account.model.response.AccountInformation
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
internal class ProfileDatasourceImplTest {

    @OptIn(DelicateCoroutinesApi::class)
    val mainThreadSurrogate = newSingleThreadContext("UI thread")

    private val api: AndroidAccountAPI = mockk(relaxed = true)
    private lateinit var dataSource: ProfileDatasource

    private val appModule = module {
        single { api }
    }

    @BeforeEach
    internal fun setUp() {
        Dispatchers.setMain(mainThreadSurrogate)
        stopKoin()
        startKoin {
            modules(appModule, profileModule(appModule))
        }
        dataSource = ProfileDatasourceImpl(api)
    }

    @AfterEach
    internal fun tearDown() {
        Dispatchers.resetMain() // reset the main dispatcher to the original Main dispatcher
        mainThreadSurrogate.close()
    }

    @Test
    fun `accountDetails - success`() = runTest {
        val accountInformation = AccountInformation(
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
        dataSource.accountDetails().test {
            val actual = awaitItem()
            assertNotNull(actual)
            assertEquals(accountInformation.username, actual.username)
        }
    }

    @Test
    fun `accountDetails - failure`() = runTest {
        coEvery { api.accountDetails(any()) } answers {
            lastArg<(AccountInformation?, List<AccountRequestError>) -> Unit>().invoke(
                null,
                emptyList(),
            )
        }
        dataSource.accountDetails().test {
            val actual = awaitItem()
            assertNull(actual)
        }
    }
}