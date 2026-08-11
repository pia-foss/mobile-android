package com.kape.profile.ui.vm

import com.kape.contracts.LogoutUseCase
import com.kape.contracts.Router
import com.kape.data.Subscribe
import com.kape.profile.domain.DeleteAccountUseCase
import com.kape.profile.domain.GetProfileUseCase
import io.mockk.coEvery
import io.mockk.coVerifyOrder
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private val useCase: GetProfileUseCase = mockk()
    private val deleteAccountUseCase: DeleteAccountUseCase = mockk()
    private val logoutUseCase: LogoutUseCase = mockk()
    private val router: Router = mockk(relaxed = true)

    private lateinit var viewModel: ProfileViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { useCase.getProfile() } returns null

        viewModel =
            ProfileViewModel(
                useCase,
                deleteAccountUseCase,
                logoutUseCase,
                router,
                testDispatcher,
            )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test logout logs out then navigates straight to Subscribe, not through Splash`() =
        runTest {
            coEvery { logoutUseCase.logout() } returns true

            viewModel.logout()

            coVerifyOrder {
                logoutUseCase.logout()
                router.updateDestination(Subscribe)
            }
        }
}