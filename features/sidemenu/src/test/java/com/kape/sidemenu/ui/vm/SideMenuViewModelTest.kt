package com.kape.sidemenu.ui.vm

import com.kape.contracts.AppInfo
import com.kape.contracts.LogoutUseCase
import com.kape.contracts.Router
import com.kape.data.LoginWithCredentials
import com.kape.profile.domain.GetProfileUseCase
import com.kape.utils.UpdateAvailableManager
import io.mockk.coEvery
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SideMenuViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private val profileUseCase: GetProfileUseCase = mockk()
    private val logoutUseCase: LogoutUseCase = mockk()
    private val appInfo: AppInfo = mockk()
    private val router: Router = mockk(relaxed = true)
    private val updateAvailableManager: UpdateAvailableManager = mockk()

    private lateinit var viewModel: SideMenuViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { profileUseCase.getProfile() } returns null
        every { updateAvailableManager.hasUpdateAvailable } returns MutableStateFlow(false)

        viewModel =
            SideMenuViewModel(
                profileUseCase,
                logoutUseCase,
                appInfo,
                router,
                updateAvailableManager,
                testDispatcher,
            )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test logout logs out then navigates straight to Login, not through Splash`() =
        runTest {
            coEvery { logoutUseCase.logout() } returns true

            viewModel.logout()

            coVerifyOrder {
                logoutUseCase.logout()
                router.updateDestination(LoginWithCredentials)
            }
        }
}