package com.kape.connection.domain

import android.app.Activity
import com.kape.payments.domain.RefreshSubscriptionStatusUseCase
import com.kape.payments.ui.VpnSubscriptionPaymentProvider
import com.kape.payments.utils.InAppMessageState
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GooglePaymentIssueHandlerImplTest {
    private val vpnSubscriptionPaymentProvider: VpnSubscriptionPaymentProvider = mockk(relaxed = true)
    private val refreshSubscriptionStatusUseCase: RefreshSubscriptionStatusUseCase = mockk(relaxed = true)
    private val inAppMessageState = MutableStateFlow<InAppMessageState>(InAppMessageState.Default)

    private fun createHandler() =
        GooglePaymentIssueHandlerImpl(
            vpnSubscriptionPaymentProvider,
            refreshSubscriptionStatusUseCase,
            CoroutineScope(UnconfinedTestDispatcher()),
        )

    @Test
    fun `checkForPaymentIssues delegates to showInAppMessages`() {
        every { vpnSubscriptionPaymentProvider.inAppMessageState } returns inAppMessageState
        val handler = createHandler()
        val activity: Activity = mockk()

        handler.checkForPaymentIssues(activity)

        verify { vpnSubscriptionPaymentProvider.showInAppMessages(activity) }
    }

    @Test
    fun `subscription status update triggers entitlement refresh`() =
        runTest {
            every { vpnSubscriptionPaymentProvider.inAppMessageState } returns inAppMessageState
            createHandler()

            inAppMessageState.value = InAppMessageState.SubscriptionStatusUpdated("purchase-token")

            coVerify { refreshSubscriptionStatusUseCase.refresh("purchase-token") }
        }
}