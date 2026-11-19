package com.kape.account.domain.usecase

import com.kape.account.domain.LoginResult
import com.kape.contracts.PurchaseHistoryState
import com.kape.contracts.StorePurchaseHistory
import com.kape.data.ComposeDestination
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.transformLatest
import org.koin.core.annotation.Factory

sealed interface ReceiptLoginProgress {
    data object Idle : ReceiptLoginProgress

    data object Loading : ReceiptLoginProgress

    data object NoPurchase : ReceiptLoginProgress

    data class Failed(
        val result: LoginResult,
    ) : ReceiptLoginProgress

    data class Succeeded(
        val destination: ComposeDestination,
    ) : ReceiptLoginProgress
}

@Factory
class LoginWithPurchaseHistoryUseCase(
    private val purchaseHistory: StorePurchaseHistory,
    private val loginWithReceipt: LoginWithReceiptUseCase,
    private val getPostLoginDestination: GetPostLoginDestinationUseCase,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(packageName: String): Flow<ReceiptLoginProgress> =
        purchaseHistory.state
            .onStart { purchaseHistory.request() }
            .transformLatest { history ->
                when (history) {
                    PurchaseHistoryState.Default -> emit(ReceiptLoginProgress.Idle)
                    PurchaseHistoryState.Failed -> emit(ReceiptLoginProgress.NoPurchase)
                    is PurchaseHistoryState.Success -> {
                        emit(ReceiptLoginProgress.Loading)
                        val result = loginWithReceipt(history.purchaseToken, history.productId, packageName)
                        if (result == LoginResult.Success) {
                            purchaseHistory.onReceiptLoginSucceeded()
                            emit(ReceiptLoginProgress.Succeeded(getPostLoginDestination()))
                        } else {
                            emit(ReceiptLoginProgress.Failed(result))
                        }
                    }
                }
            }
}