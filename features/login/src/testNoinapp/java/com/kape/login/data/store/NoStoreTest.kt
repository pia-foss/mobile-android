package com.kape.login.data.store

import com.kape.contracts.PurchaseHistoryState
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class NoStoreTest {
    @Test
    fun `without a store there is never a purchase to log in with`() =
        runTest {
            val history = NoStorePurchaseHistory()

            history.request()
            history.onReceiptLoginSucceeded()
            NoStoreDataCleaner().clear()

            assertEquals(PurchaseHistoryState.Default, history.state.value)
        }
}