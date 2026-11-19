package com.kape.signup.data

import android.app.Activity
import com.kape.signup.domain.StoreOffer
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

internal class NoInAppSubscriptionStoreTest {
    private val store = NoInAppSubscriptionStore(offerInstead = StoreOffer.WebsiteOnly)

    private val activity = Activity()

    @Test
    fun `offers the alternative once plans are requested`() {
        assertEquals(StoreOffer.None, store.offer.value)

        store.loadPlans(activity)

        assertEquals(StoreOffer.WebsiteOnly, store.offer.value)
    }

    @Test
    fun `splash loading prices offers the website too`() {
        store.loadPrices(activity)

        assertEquals(StoreOffer.WebsiteOnly, store.offer.value)
    }

    @Test
    fun `never has a subscription or a purchase to resume`() =
        runTest {
            assertFalse(store.hasActiveSubscription().first())
            assertFalse(store.hasResumablePurchase())
        }
}