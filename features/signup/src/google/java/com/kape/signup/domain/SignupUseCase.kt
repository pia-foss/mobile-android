package com.kape.signup.domain

import com.kape.login.domain.mobile.LoginUseCase
import com.kape.login.utils.LoginState
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

    suspend fun vpnSignup(email: String): Flow<Credentials?> = flow {
        val purchaseData = purchaseDetailsUseCase.getPurchaseDetails()
        if (purchaseData == null) {
            emit(null)
            return@flow
        }
        val obfuscatedDeviceIdentifier = getObfuscatedDeviceIdentifierUseCase.obfuscatedDeviceIdentifier().getOrElse {
            emit(null)
            return@flow
        }
        signupDataSource.vpnSignup(purchaseData.orderId, purchaseData.token, purchaseData.productId, obfuscatedDeviceIdentifier)
            .collect { credentials ->
                credentials?.let { data ->
                    loginUseCase.login(data.username, data.password).collect { loginState ->
                        if (loginState == LoginState.Successful) {
                            emailDataSource.setEmail(email).collect { successful ->
                                if (successful) {
                                    emit(credentials)
                                } else {
                                    emit(null)
                                }
                            }
                        } else {
                            // TODO: emit error
                            emit(null)
                        }
                    }
                } ?: emit(null)
            }
    }
}