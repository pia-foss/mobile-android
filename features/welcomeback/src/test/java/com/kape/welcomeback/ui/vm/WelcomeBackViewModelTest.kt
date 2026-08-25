package com.kape.welcomeback.ui.vm

import com.kape.contracts.Router
import com.kape.data.Connection
import com.kape.data.LoginWithCredentials
import com.kape.data.TvLoginUsername
import com.kape.login.domain.mobile.LoginUseCase
import com.kape.login.domain.mobile.LoginWithReceiptHandler
import com.kape.login.utils.IDLE
import com.kape.login.utils.LOADING
import com.kape.login.utils.LoginState
import com.kape.login.utils.RECEIPT_FAILED
import com.kape.payments.utils.PurchaseHistoryState
import com.kape.permissions.utils.PermissionUtil
import com.kape.shareevents.data.KpiEventGenerator
import com.kape.shareevents.domain.SubmitKpiEventUseCase
import com.kape.utils.PlatformUtils
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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
import kotlin.test.assertEquals

private const val PACKAGE_NAME = "com.kape.pia"
private const val PURCHASE_TOKEN = "purchase-token"
private const val PRODUCT_ID = "product-id"

@OptIn(ExperimentalCoroutinesApi::class)
class WelcomeBackViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val router: Router = mockk(relaxed = true)
    private val loginUseCase: LoginUseCase = mockk()
    private val loginWithReceiptHandler: LoginWithReceiptHandler = mockk()
    private val permissionsUtil: PermissionUtil = mockk()
    private val submitKpiEventUseCase: SubmitKpiEventUseCase = mockk(relaxed = true)
    private val eventGenerator: KpiEventGenerator = mockk(relaxed = true)
    private val platformUtils: PlatformUtils = mockk()
    private val purchaseHistoryState = MutableStateFlow<PurchaseHistoryState>(PurchaseHistoryState.Default)

    private lateinit var viewModel: WelcomeBackViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        every { loginWithReceiptHandler.purchaseHistoryState } returns purchaseHistoryState

        viewModel =
            WelcomeBackViewModel(
                router,
                loginUseCase,
                loginWithReceiptHandler,
                permissionsUtil,
                submitKpiEventUseCase,
                eventGenerator,
                platformUtils,
                testDispatcher,
            )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
        purchaseHistoryState.value = PurchaseHistoryState.Default
    }

    @Test
    fun `onUsernameAndPasswordClicked navigates to LoginWithCredentials on mobile`() {
        every { platformUtils.isTv() } returns false

        viewModel.onUsernameAndPasswordClicked()

        verify { router.updateDestination(LoginWithCredentials) }
    }

    @Test
    fun `onUsernameAndPasswordClicked navigates to TvLoginUsername on TV`() {
        every { platformUtils.isTv() } returns true

        viewModel.onUsernameAndPasswordClicked()

        verify { router.updateDestination(TvLoginUsername) }
    }

    @Test
    fun `onPlayStoreAccountClicked logs in with the receipt and navigates on success`() =
        runTest {
            every { loginWithReceiptHandler.getPurchaseHistory() } answers {
                purchaseHistoryState.value = PurchaseHistoryState.PurchaseHistorySuccess(PURCHASE_TOKEN, PRODUCT_ID)
            }
            coEvery { loginUseCase.loginWithReceipt(PURCHASE_TOKEN, PRODUCT_ID, PACKAGE_NAME) } returns LoginState.Successful
            every { permissionsUtil.getNextDestination() } returns Connection

            viewModel.onPlayStoreAccountClicked(PACKAGE_NAME)

            coVerify(exactly = 1) { submitKpiEventUseCase.submitEvent(any()) }
            verify { router.updateDestination(Connection) }
            // No terminal state is emitted after a successful login — the screen navigates away
            // while still showing the loading indicator, matching LoginViewModel.loginWithReceipt().
            assertEquals(LOADING, viewModel.state.value)
        }

    @Test
    fun `onPlayStoreAccountClicked surfaces a receipt error when login fails`() =
        runTest {
            every { loginWithReceiptHandler.getPurchaseHistory() } answers {
                purchaseHistoryState.value = PurchaseHistoryState.PurchaseHistorySuccess(PURCHASE_TOKEN, PRODUCT_ID)
            }
            coEvery { loginUseCase.loginWithReceipt(PURCHASE_TOKEN, PRODUCT_ID, PACKAGE_NAME) } returns LoginState.Failed

            viewModel.onPlayStoreAccountClicked(PACKAGE_NAME)

            assertEquals(RECEIPT_FAILED, viewModel.state.value)
            verify(exactly = 0) { router.updateDestination(any()) }
            coVerify(exactly = 0) { submitKpiEventUseCase.submitEvent(any()) }
        }

    @Test
    fun `onPlayStoreAccountClicked surfaces a receipt error when the purchase history lookup fails`() =
        runTest {
            every { loginWithReceiptHandler.getPurchaseHistory() } answers {
                purchaseHistoryState.value = PurchaseHistoryState.PurchaseHistoryFailed
            }

            viewModel.onPlayStoreAccountClicked(PACKAGE_NAME)

            assertEquals(RECEIPT_FAILED, viewModel.state.value)
            coVerify(exactly = 0) { loginUseCase.loginWithReceipt(any(), any(), any()) }
            verify(exactly = 0) { router.updateDestination(any()) }
        }

    @Test
    fun `onPlayStoreAccountClicked resets to idle when there is no purchase history yet`() =
        runTest {
            every { loginWithReceiptHandler.getPurchaseHistory() } returns Unit

            viewModel.onPlayStoreAccountClicked(PACKAGE_NAME)

            assertEquals(IDLE, viewModel.state.value)
        }
}