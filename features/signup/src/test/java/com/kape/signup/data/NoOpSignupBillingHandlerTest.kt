package com.kape.signup.data

import com.kape.signup.utils.DEFAULT
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse

class NoOpSignupBillingHandlerTest {
    private val handler = NoOpSignupBillingHandler(DEFAULT)

    @Test
    fun `test hasResumablePurchase is always false - flavors without in-app purchase have nothing to resume`() =
        runTest {
            assertFalse(handler.hasResumablePurchase())
        }
}