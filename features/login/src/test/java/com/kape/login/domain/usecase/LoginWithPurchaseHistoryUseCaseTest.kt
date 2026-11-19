package com.kape.login.domain.usecase

import com.kape.account.domain.LoginResult
import com.kape.account.domain.usecase.GetPostLoginDestinationUseCase
import com.kape.account.domain.usecase.LoginWithPurchaseHistoryUseCase
import com.kape.account.domain.usecase.LoginWithReceiptUseCase
import com.kape.account.domain.usecase.ReceiptLoginProgress
import com.kape.contracts.PurchaseHistoryState
import com.kape.contracts.StorePurchaseHistory
import com.kape.data.Connection
import com.kape.login.FakeAuthentication
import com.kape.login.FakePermissions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class LoginWithPurchaseHistoryUseCaseTest {
    private val authentication = FakeAuthentication()
    private val history = FakePurchaseHistory()
    private val useCase =
        LoginWithPurchaseHistoryUseCase(
            history,
            LoginWithReceiptUseCase(authentication),
            GetPostLoginDestinationUseCase(FakePermissions()),
        )

    @Test
    fun `asks the store and logs in with the purchase found`() =
        runTest {
            history.onRequest = { PurchaseHistoryState.Success("token", "yearly") }

            val progress = useCase("com.kape.vpn").take(2).toList()

            assertEquals(listOf(ReceiptLoginProgress.Loading, ReceiptLoginProgress.Succeeded(Connection)), progress)
            assertEquals(1, history.successesReported)
        }

    @Test
    fun `reports a rejected receipt`() =
        runTest {
            authentication.result = LoginResult.Failed
            history.onRequest = { PurchaseHistoryState.Success("token", "yearly") }

            val progress = useCase("com.kape.vpn").take(2).toList()

            assertEquals(ReceiptLoginProgress.Failed(LoginResult.Failed), progress.last())
            assertEquals(0, history.successesReported)
        }

    @Test
    fun `reports when the store has no purchase`() =
        runTest {
            history.onRequest = { PurchaseHistoryState.Failed }

            assertEquals(listOf(ReceiptLoginProgress.NoPurchase), useCase("com.kape.vpn").take(1).toList())
        }
}

private class FakePurchaseHistory : StorePurchaseHistory {
    override val state = MutableStateFlow<PurchaseHistoryState>(PurchaseHistoryState.Default)
    var onRequest: () -> PurchaseHistoryState = { PurchaseHistoryState.Default }
    var successesReported = 0

    override fun request() {
        state.value = onRequest()
    }

    override suspend fun onReceiptLoginSucceeded() {
        successesReported++
    }
}