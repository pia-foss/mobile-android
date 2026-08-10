package com.kape.payments.domain

import android.content.Context
import com.kape.contracts.AuthenticationDataSource
import com.kape.data.model.PurchaseData
import com.kape.payments.prefs.SubscriptionPrefs
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RefreshSubscriptionStatusUseCaseTest {
    private val authenticationDataSource: AuthenticationDataSource = mockk(relaxed = true)
    private val prefs: SubscriptionPrefs = mockk()
    private val context: Context = mockk()
    private lateinit var useCase: RefreshSubscriptionStatusUseCase

    @BeforeEach
    fun setUp() {
        useCase = RefreshSubscriptionStatusUseCase(authenticationDataSource, prefs, context)
    }

    @Test
    fun `refresh calls loginWithReceipt with cached productId and package name when purchase data exists`() =
        runTest {
            every { prefs.vpnPurchaseData.value } returns PurchaseData("cached-token", "productId", "orderId")
            every { context.packageName } returns "com.kape.vpn"

            useCase.refresh("new-token")

            coVerify { authenticationDataSource.loginWithReceipt("new-token", "productId", "com.kape.vpn") }
        }

    @Test
    fun `refresh does nothing when no cached purchase data exists`() =
        runTest {
            every { prefs.vpnPurchaseData.value } returns null

            useCase.refresh("new-token")

            coVerify(exactly = 0) { authenticationDataSource.loginWithReceipt(any(), any(), any()) }
        }
}