package com.kape.signup.domain

import com.kape.payments.domain.GetPurchaseDetailsUseCase
import com.kape.signup.models.Credentials
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SignupUseCase(private val signupDataSource: SignupDataSource, private val purchaseDetailsUseCase: GetPurchaseDetailsUseCase) {

    suspend fun signup(): Flow<Credentials?> = flow {
        val purchaseData = purchaseDetailsUseCase.getPurchaseDetails()
        if (purchaseData == null) {
            emit(null)
            return@flow
        }
        signupDataSource.signup(purchaseData.userId, purchaseData.receiptId).collect {
            emit(it)
        }
    }
}