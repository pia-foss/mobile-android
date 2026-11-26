package com.kape.profile.ui.vm

import com.kape.account.domain.AccountDetails
import com.kape.account.domain.AccountDetailsRepository
import com.kape.account.domain.usecase.GetAccountDetailsUseCase
import com.kape.contracts.LogoutUseCase
import com.kape.contracts.Router
import com.kape.data.Splash
import com.kape.data.WebDestination
import io.mockk.coVerifyOrder
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class ProfileViewModelTest {
    private val dispatcher: TestDispatcher = StandardTestDispatcher()
    private var account: AccountDetails? = AccountDetails("p1234567", isExpired = false, daysRemaining = 30, showExpiryWarning = false)
    private val repository =
        object : AccountDetailsRepository {
            override suspend fun getAccountDetails() = account
        }
    private val logout = mockk<LogoutUseCase>(relaxed = true)
    private val router = mockk<Router>(relaxed = true)

    private fun viewModel() = ProfileViewModel(GetAccountDetailsUseCase(repository), logout, router)

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loads while the account is fetched, then shows it`() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            assertTrue(viewModel.state.value.isLoading)

            advanceUntilIdle()

            val state = viewModel.state.value
            assertFalse(state.isLoading)
            assertEquals("p1234567", state.username)
            assertNotNull(state.expiresAtMillis)
        }

    @Test
    fun `stops loading when the account can't be fetched`() =
        runTest(dispatcher) {
            account = null
            val viewModel = viewModel()

            advanceUntilIdle()

            assertFalse(viewModel.state.value.isLoading)
            assertNull(viewModel.state.value.expiresAtMillis)
        }

    @Test
    fun `logging out returns to the splash screen afterwards`() =
        runTest(dispatcher) {
            val viewModel = viewModel()

            viewModel.logout()
            advanceUntilIdle()

            coVerifyOrder {
                logout.logout()
                router.updateDestination(Splash)
            }
        }

    @Test
    fun `account deletion opens the website`() {
        viewModel().navigateToDeleteAccount()

        verify { router.updateDestination(WebDestination.DeleteAccount) }
    }
}