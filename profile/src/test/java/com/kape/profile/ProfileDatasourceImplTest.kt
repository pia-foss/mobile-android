package com.kape.profile

import android.text.format.DateFormat
import com.kape.profile.data.ProfileDatasourceImpl
import com.kape.profile.di.profileModule
import com.kape.profile.domain.ProfileDatasource
import com.kape.profile.models.Subscription
import com.privateinternetaccess.account.AccountRequestError
import com.privateinternetaccess.account.AndroidAccountAPI
import com.privateinternetaccess.account.model.response.AccountInformation
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
class ProfileDatasourceImplTest : KoinTest {

    private val api: AndroidAccountAPI = mockk(relaxed = true)
    private lateinit var dataSource: ProfileDatasource

    private val appModule = module {
        single { api }
    }

    @BeforeEach
    internal fun setUp() {
        stopKoin()
        startKoin {
            modules(appModule, profileModule)
        }
        dataSource = ProfileDatasourceImpl()
    }

    @Test
    suspend fun `test ProfileDatasourceImpl converts AccountInformation to Profile`() = runTest {
        val username = "John Doe"
        val isRenewing = true
        val daysRemaining = 0
        val now = System.currentTimeMillis()
        val today = DateFormat.format(Subscription.DATE_FORMAT, now).toString()

        // given
        val mockedAccount: AccountInformation = mockk()
        every { mockedAccount.username } returns username
        every { mockedAccount.recurring } returns isRenewing
        every { mockedAccount.daysRemaining } returns daysRemaining
        coEvery { api.accountDetails(any()) } answers {
            firstArg<(AccountInformation, List<AccountRequestError>) -> Unit>().invoke(mockedAccount, listOf())
        }

        // when
        dataSource.accountDetails().collect { profile ->

            // then
            assertEquals(username, profile.username)
            assertEquals(isRenewing, profile.subscription.isRenewing)
            assertEquals(today, profile.subscription.expirationDate)
        }
    }
}