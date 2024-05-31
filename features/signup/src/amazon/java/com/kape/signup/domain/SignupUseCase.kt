package com.kape.signup.domain

import com.kape.login.domain.mobile.LoginUseCase
import com.kape.payments.domain.GetPurchaseDetailsUseCase
import com.kape.signup.data.models.Credentials
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SignupUseCase(
    private val signupDataSource: SignupDataSource,
    private val loginUseCase: LoginUseCase,
    private val emailDataSource: EmailDataSource,
    private val purchaseDetailsUseCase: GetPurchaseDetailsUseCase,
) {

    suspend fun signup(email: String): Flow<Credentials?> = flow {
        val purchaseData = purchaseDetailsUseCase.getPurchaseDetails()
        if (purchaseData == null) {
            emit(null)
            return@flow
        }
        signupDataSource.signup(purchaseData.userId, purchaseData.receiptId, email).collect {
            emit(it)
        }
    }
}