package com.kape.signup.ui.vm

import android.app.Activity
import com.kape.contracts.Router
import com.kape.permissions.utils.PermissionUtil
import com.kape.shareevents.data.KpiEventGenerator
import com.kape.shareevents.domain.SubmitKpiEventUseCase
import com.kape.signup.domain.ConsentUseCase
import com.kape.signup.domain.SignupBillingHandler
import com.kape.signup.domain.SignupHandler
import com.kape.signup.utils.SignupStep
import com.kape.utils.NetworkConnectionListener
import com.kape.utils.PlatformUtils
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class SignupViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private val router: Router = mockk(relaxed = true)
    private val billingHandler: SignupBillingHandler = mockk()
    private val consentUseCase: ConsentUseCase = mockk()
    private val signupHandler: SignupHandler = mockk()
    private val permissionUtil: PermissionUtil = mockk()
    private val submitKpiEventUseCase: SubmitKpiEventUseCase = mockk(relaxed = true)
    private val platformUtils: PlatformUtils = mockk()
    private val eventGenerator: KpiEventGenerator = mockk(relaxed = true)
    private val networkConnectionListener: NetworkConnectionListener = mockk()
    private val activity: Activity = mockk()

    private lateinit var viewModel: SignupViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        every { billingHandler.billingState } returns emptyFlow()
        every { billingHandler.initialize(any()) } returns Unit
        every { networkConnectionListener.isConnected } returns MutableStateFlow(true)

        viewModel =
            SignupViewModel(
                router,
                billingHandler,
                consentUseCase,
                signupHandler,
                permissionUtil,
                submitKpiEventUseCase,
                platformUtils,
                eventGenerator,
                testDispatcher,
                testDispatcher,
                networkConnectionListener,
            )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test loadPrices resumes at Email when a purchase is pending, regardless of consent decision`() =
        runTest {
            coEvery { billingHandler.hasResumablePurchase() } returns true

            viewModel.loadPrices(activity)

            assertEquals(SignupStep.Email, viewModel.state.value.step)
            coVerify(exactly = 0) { billingHandler.loadPrices(any(), any(), any(), any()) }
            coVerify(exactly = 0) { consentUseCase.hasMadeConsentDecision() }
        }

    @Test
    fun `test loadPrices falls through to the normal billing flow when there is no pending purchase`() =
        runTest {
            coEvery { billingHandler.hasResumablePurchase() } returns false
            every { billingHandler.loadPrices(any(), any(), any(), any()) } returns Unit

            viewModel.loadPrices(activity)

            coVerify(exactly = 1) { billingHandler.loadPrices(any(), testDispatcher, testDispatcher, activity) }
            coVerify(exactly = 0) { consentUseCase.hasMadeConsentDecision() }
        }
}