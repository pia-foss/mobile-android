package com.kape.account.domain.usecase

import com.kape.account.domain.AuthenticationRepository
import com.kape.account.domain.LoginResult
import org.koin.core.annotation.Factory

@Factory
class LoginWithReceiptUseCase(
    private val repository: AuthenticationRepository,
) {
    suspend operator fun invoke(
        purchaseToken: String,
        productId: String,
        packageName: String,
    ): LoginResult = repository.loginWithReceipt(purchaseToken, productId, packageName)
}