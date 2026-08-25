package com.kape.signup.data

import android.app.Activity
import com.kape.localprefs.prefs.ConsentPrefs
import com.kape.payments.domain.GetSubscriptionsUseCase
import com.kape.payments.prefs.SubscriptionPrefs
import com.kape.payments.ui.VpnSubscriptionPaymentProvider
import com.kape.payments.utils.PurchaseState
import com.kape.shareevents.data.KpiEventGenerator
import com.kape.shareevents.domain.SubmitKpiEventUseCase
import com.kape.ui.utils.PriceFormatter
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
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
internal class GoogleSignupBillingHandlerTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val vpnSubscriptionPaymentProvider: VpnSubscriptionPaymentProvider = mockk(relaxed = true)
    private val subscriptionPrefs: SubscriptionPrefs = mockk(relaxed = true)
    private val subscriptionsUseCase: GetSubscriptionsUseCase = mockk(relaxed = true)
    private val formatter: PriceFormatter = mockk(relaxed = true)
    private val submitEventUseCase: SubmitKpiEventUseCase = mockk(relaxed = true)
    private val consentPrefs: ConsentPrefs = mockk(relaxed = true)
    private val eventGenerator: KpiEventGenerator = mockk(relaxed = true)
    private val activity: Activity = mockk()
    private val purchaseState = MutableStateFlow<PurchaseState>(PurchaseState.Default)

    private lateinit var handler: GoogleSignupBillingHandler

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        every { vpnSubscriptionPaymentProvider.purchaseState } returns purchaseState

        handler =
            GoogleSignupBillingHandler(
                vpnSubscriptionPaymentProvider,
                subscriptionPrefs,
                subscriptionsUseCase,
                formatter,
                submitEventUseCase,
                consentPrefs,
                eventGenerator,
                CoroutineScope(testDispatcher),
            )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
        purchaseState.value = PurchaseState.Default
    }

    @Test
    fun `registerAndAwaitReady skips registration when the client is already registered`() =
        runTest {
            every { vpnSubscriptionPaymentProvider.isClientRegistered() } returns true

            handler.registerAndAwaitReady(testDispatcher, activity)

            verify(exactly = 0) { vpnSubscriptionPaymentProvider.register(any(), any()) }
        }

    @Test
    fun `registerAndAwaitReady registers and returns once the client reports InitSuccess`() =
        runTest {
            every { vpnSubscriptionPaymentProvider.isClientRegistered() } returns false
            every { vpnSubscriptionPaymentProvider.register(activity, any()) } answers {
                purchaseState.value = PurchaseState.InitSuccess
            }

            handler.registerAndAwaitReady(testDispatcher, activity)

            verify(exactly = 1) { vpnSubscriptionPaymentProvider.register(activity, any()) }
        }

    @Test
    fun `registerAndAwaitReady returns without hanging when the client reports InitFailed`() =
        runTest {
            every { vpnSubscriptionPaymentProvider.isClientRegistered() } returns false
            every { vpnSubscriptionPaymentProvider.register(activity, any()) } answers {
                purchaseState.value = PurchaseState.InitFailed
            }

            // Reaching the end of this test (rather than it hanging) proves
            // registerAndAwaitReady resumes on a definitive failure signal too, not just success.
            handler.registerAndAwaitReady(testDispatcher, activity)
        }

    @Test
    fun `registerAndAwaitReady gives up after the timeout if the client never becomes ready`() =
        runTest {
            every { vpnSubscriptionPaymentProvider.isClientRegistered() } returns false
            every { vpnSubscriptionPaymentProvider.register(activity, any()) } returns Unit

            // purchaseState never emits a terminal value here — reaching the end of this test
            // (via runTest's virtual-time auto-advance) proves the internal withTimeoutOrNull
            // fallback fires instead of suspending forever.
            handler.registerAndAwaitReady(testDispatcher, activity)
        }
}