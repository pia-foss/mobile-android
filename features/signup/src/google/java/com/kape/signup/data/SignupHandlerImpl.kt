package com.kape.signup.data

import com.kape.signup.data.models.Credentials
import com.kape.signup.domain.SignupHandler
import com.kape.signup.domain.SignupUseCase

class SignupHandlerImpl(
    private val signupUseCase: SignupUseCase,
) : SignupHandler {
    override suspend fun vpnSignup(email: String): Credentials? = signupUseCase.vpnSignup(email)
}