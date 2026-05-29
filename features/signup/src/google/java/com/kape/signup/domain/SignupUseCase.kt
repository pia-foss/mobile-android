package com.kape.signup.domain

import com.kape.login.domain.mobile.LoginUseCase
import com.kape.login.utils.LoginState
import com.kape.payments.domain.GetPurchaseDetailsUseCase
import com.kape.signup.data.models.Credentials
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import org.koin.core.annotation.Singleton

@Singleton
class SignupUseCase(
    private val signupDataSource: SignupDataSource,
    private val loginUseCase: LoginUseCase,
    private val emailDataSource: EmailDataSource,
    private val purchaseDetailsUseCase: GetPurchaseDetailsUseCase,
    private val getObfuscatedDeviceIdentifierUseCase: GetObfuscatedDeviceIdentifierUseCase,
) {
    suspend fun vpnSignup(email: String): Credentials? {
        val purchaseData =
            withTimeoutOrNull(5000) {
                purchaseDetailsUseCase.getPurchaseDetails().filterNotNull().first()
            } ?: return null
        val obfuscatedDeviceIdentifier =
            getObfuscatedDeviceIdentifierUseCase.obfuscatedDeviceIdentifier().getOrElse {
                return null
            }
        val credentials =
            signupDataSource.vpnSignup(
                purchaseData.orderId,
                purchaseData.token,
                purchaseData.productId,
                obfuscatedDeviceIdentifier,
            ) ?: return null
        val loginState = loginUseCase.login(credentials.username, credentials.password)
        return if (loginState == LoginState.Successful) {
            val successful = emailDataSource.setEmail(email)
            if (successful) credentials else null
        } else {
            null
        }
    }
}