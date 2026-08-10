package com.kape.signup.data

import com.kape.data.model.PurchaseData
import com.kape.localprefs.prefs.ConsentPrefs
import com.kape.payments.domain.GetSubscriptionsUseCase
import com.kape.payments.prefs.SubscriptionPrefs
import com.kape.payments.ui.VpnSubscriptionPaymentProvider
import com.kape.shareevents.domain.SubmitKpiEventUseCase
import com.kape.ui.utils.PriceFormatter
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.test.assertEquals

class GoogleSignupBillingHandlerTest {
    private val vpnSubscriptionPaymentProvider: VpnSubscriptionPaymentProvider = mockk()
    private val subscriptionPrefs: SubscriptionPrefs = mockk()
    private val subscriptionsUseCase: GetSubscriptionsUseCase = mockk()
    private val formatter: PriceFormatter = mockk()
    private val submitEventUseCase: SubmitKpiEventUseCase = mockk()
    private val consentPrefs: ConsentPrefs = mockk()

    private lateinit var handler: GoogleSignupBillingHandler

    @BeforeEach
    fun setUp() {
        handler =
            GoogleSignupBillingHandler(
                vpnSubscriptionPaymentProvider,
                subscriptionPrefs,
                subscriptionsUseCase,
                formatter,
                submitEventUseCase,
                consentPrefs,
            )
    }

    @ParameterizedTest(name = "purchase: {0}, expected: {1}")
    @MethodSource("arguments")
    fun `test hasResumablePurchase reads the one-shot purchase data`(
        purchaseData: PurchaseData?,
        expected: Boolean,
    ) = runTest {
        coEvery { subscriptionPrefs.getVpnPurchaseDataOnce() } returns purchaseData

        val actual = handler.hasResumablePurchase()

        assertEquals(expected, actual)
    }

    companion object {
        @JvmStatic
        fun arguments() =
            Stream.of(
                Arguments.of(PurchaseData("token", "productId", "orderId"), true),
                Arguments.of(null, false),
            )
    }
}