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
    private val getObfuscatedDeviceIdentifierUseCase: GetObfuscatedDeviceIdentifierUseCase,
) {

    fun vpnSignup(email: String): Flow<Credentials?> = flow {
        emit(null)
    }
}