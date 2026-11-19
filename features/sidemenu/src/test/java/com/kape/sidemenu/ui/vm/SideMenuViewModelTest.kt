package com.kape.sidemenu.ui.vm

import com.kape.account.domain.AccountDetails
import com.kape.account.domain.AccountDetailsRepository
import com.kape.account.domain.usecase.GetAccountDetailsUseCase
import com.kape.contracts.AppInfo
import com.kape.contracts.AppUpdateStatus
import com.kape.contracts.LogoutUseCase
import com.kape.contracts.Router
import com.kape.data.Splash
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class SideMenuViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private var account: AccountDetails? = AccountDetails("p1234567", isExpired = false, daysRemaining = 30, showExpiryWarning = false)
    private var fetches = 0
    private val repository =
        object : AccountDetailsRepository {
            override suspend fun getAccountDetails(): AccountDetails? {
                fetches++
                return account
            }
        }
    private val logout = mockk<LogoutUseCase>(relaxed = true)
    private val router = mockk<Router>(relaxed = true)

    private fun viewModel() =
        SideMenuViewModel(
            GetAccountDetailsUseCase(repository),
            logout,
            router,
            mockk<AppUpdateStatus> { every { hasUpdateAvailable } returns MutableStateFlow(false) },
            mockk<AppInfo> {
                every { versionName } returns "4.0.0"
                every { versionCode } returns 400
            },
        )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `shows the username in capitals and the app version`() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            advanceUntilIdle()

            assertEquals("P1234567", viewModel.username.value)
            assertEquals("4.0.0", viewModel.versionName)
            assertEquals("400", viewModel.versionCode)
        }

    @Test
    fun `opening the drawer again does not refetch a known username`() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            advanceUntilIdle()

            viewModel.refreshUsername()
            advanceUntilIdle()

            assertEquals(1, fetches)
        }

    @Test
    fun `opening the drawer retries a username that failed to load`() =
        runTest(dispatcher) {
            account = null
            val viewModel = viewModel()
            advanceUntilIdle()
            account = AccountDetails("p7654321", isExpired = false, daysRemaining = 1, showExpiryWarning = true)

            viewModel.refreshUsername()
            advanceUntilIdle()

            assertEquals("P7654321", viewModel.username.value)
        }

    @Test
    fun `logging out clears the username and returns to the splash screen`() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            advanceUntilIdle()

            viewModel.logout()
            advanceUntilIdle()

            assertEquals("", viewModel.username.value)
            coVerifyOrder {
                logout.logout()
                router.updateDestination(Splash)
            }
        }
}