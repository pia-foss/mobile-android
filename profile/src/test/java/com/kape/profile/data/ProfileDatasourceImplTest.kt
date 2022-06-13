package com.kape.profile.data

import android.text.format.DateFormat
import app.cash.turbine.test
import com.kape.profile.di.profileModule
import com.kape.profile.domain.ProfileDatasource
import com.kape.profile.models.Subscription
import com.privateinternetaccess.account.AccountRequestError
import com.privateinternetaccess.account.AndroidAccountAPI
import com.privateinternetaccess.account.model.response.AccountInformation
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

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
            modules(appModule, profileModule)
        }
        dataSource = ProfileDatasourceImpl()
    }

    @AfterEach
    internal fun tearDown() {
        Dispatchers.resetMain() // reset the main dispatcher to the original Main dispatcher
        mainThreadSurrogate.close()
    }

    @Test
    fun `test ProfileDatasourceImpl converts AccountInformation to Profile`() = runTest {
        val username = "John Doe"
        val isRenewing = true
        val daysRemaining = 0
        val now = System.currentTimeMillis()
        val today = Subscription.DATE_FORMAT.format(now)

        // given
        val mockedAccount: AccountInformation = mockk()
        every { mockedAccount.username } returns username
        every { mockedAccount.recurring } returns isRenewing
        every { mockedAccount.daysRemaining } returns daysRemaining
        coEvery { api.accountDetails(any()) } answers {
            firstArg<(AccountInformation, List<AccountRequestError>) -> Unit>().invoke(mockedAccount, listOf())
        }

        // when
        dataSource.accountDetails().test {
            val profile = awaitItem()
            // then
            assertEquals(username, profile.username)
            assertEquals(isRenewing, profile.subscription.isRenewing)
            assertEquals(today, profile.subscription.expirationDate)
        }
    }
}