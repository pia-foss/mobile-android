package com.kape.payments.ui

import android.app.Activity
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.InAppMessageResponseListener
import com.android.billingclient.api.InAppMessageResult
import com.kape.payments.prefs.SubscriptionPrefs
import com.kape.payments.utils.InAppMessageState
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class VpnSubscriptionPaymentProviderImplTest {
    private val prefs: SubscriptionPrefs = mockk(relaxed = true)
    private val billingClient: BillingClient = mockk(relaxed = true)
    private lateinit var provider: VpnSubscriptionPaymentProviderImpl

    @BeforeEach
    fun setUp() {
        provider = VpnSubscriptionPaymentProviderImpl(prefs, ioScope = CoroutineScope(UnconfinedTestDispatcher()))

        val field = VpnSubscriptionPaymentProviderImpl::class.java.getDeclaredField("billingClient")
        field.isAccessible = true
        field.set(provider, billingClient)
    }

    @Test
    fun `showInAppMessages updates state to SubscriptionStatusUpdated when billing returns that response code`() {
        val result: InAppMessageResult = mockk()
        every { result.responseCode } returns InAppMessageResult.InAppMessageResponseCode.SUBSCRIPTION_STATUS_UPDATED
        every { result.purchaseToken } returns "purchase-token"
        every { billingClient.isReady } returns true
        every { billingClient.showInAppMessages(any(), any(), any()) } answers {
            thirdArg<InAppMessageResponseListener>().onInAppMessageResponse(result)
            mockk<BillingResult>()
        }

        provider.showInAppMessages(mockk<Activity>())

        val state = provider.inAppMessageState.value
        assertTrue(state is InAppMessageState.SubscriptionStatusUpdated)
        assertEquals("purchase-token", state.purchaseToken)
    }

    @Test
    fun `showInAppMessages updates state to NoActionNeeded when billing returns that response code`() {
        val result: InAppMessageResult = mockk()
        every { result.responseCode } returns InAppMessageResult.InAppMessageResponseCode.NO_ACTION_NEEDED
        every { result.purchaseToken } returns null
        every { billingClient.isReady } returns true
        every { billingClient.showInAppMessages(any(), any(), any()) } answers {
            thirdArg<InAppMessageResponseListener>().onInAppMessageResponse(result)
            mockk<BillingResult>()
        }

        provider.showInAppMessages(mockk<Activity>())

        assertEquals(InAppMessageState.NoActionNeeded, provider.inAppMessageState.value)
    }

    @Test
    fun `isClientRegistered reflects billing client readiness`() {
        every { billingClient.isReady } returns true
        assertTrue(provider.isClientRegistered())

        every { billingClient.isReady } returns false
        assertFalse(provider.isClientRegistered())
    }
}