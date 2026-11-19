package com.kape.login.data.store

import com.kape.contracts.PurchaseEvents
import com.kape.contracts.PurchaseHistoryState
import com.kape.payments.domain.VpnSubscriptionBilling
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.lang.reflect.Proxy

internal class PlayStoreTest {
    private val billing = FakeBilling()
    private val events = FakePurchaseEvents()

    @Test
    fun `the purchase history comes from Play Billing and a receipt login counts as a purchase`() =
        runTest {
            val history = PlayStorePurchaseHistory(billing, events)

            history.request()
            billing.purchaseHistory.value = PurchaseHistoryState.Success("token", "yearly")
            history.onReceiptLoginSucceeded()

            assertEquals(PurchaseHistoryState.Success("token", "yearly"), history.state.value)
            assertEquals(listOf("requestPurchaseHistory"), billing.calls)
            assertEquals(listOf("purchaseSucceeded"), events.events)
        }

    @Test
    fun `logging out clears the stored billing data`() =
        runTest {
            PlayStoreDataCleaner(billing).clear()

            assertEquals(listOf("clearStoredData"), billing.calls)
        }
}

private class FakeBilling : VpnSubscriptionBilling by unsupported() {
    override val purchaseHistory = MutableStateFlow<PurchaseHistoryState>(PurchaseHistoryState.Default)
    val calls = mutableListOf<String>()

    override fun requestPurchaseHistory() {
        calls += "requestPurchaseHistory"
    }

    override suspend fun clearStoredData() {
        calls += "clearStoredData"
    }
}

private class FakePurchaseEvents : PurchaseEvents {
    val events = mutableListOf<String>()

    override suspend fun purchaseStarted() {
        events += "purchaseStarted"
    }

    override suspend fun purchaseSucceeded() {
        events += "purchaseSucceeded"
    }

    override suspend fun purchaseFailed(reason: String) {
        events += "purchaseFailed($reason)"
    }

    override suspend fun signupRetried(
        attempt: Int,
        error: String,
    ) {
        events += "signupRetried($attempt)"
    }
}

private fun unsupported(): VpnSubscriptionBilling =
    Proxy.newProxyInstance(
        VpnSubscriptionBilling::class.java.classLoader,
        arrayOf(VpnSubscriptionBilling::class.java),
    ) { _, method, _ -> throw UnsupportedOperationException(method.name) } as VpnSubscriptionBilling