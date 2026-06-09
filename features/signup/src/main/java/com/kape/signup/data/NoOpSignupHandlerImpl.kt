package com.kape.signup.data

import com.kape.signup.data.models.Credentials
import com.kape.signup.domain.SignupHandler

class NoOpSignupHandlerImpl : SignupHandler {
    override suspend fun vpnSignup(email: String): Credentials? = null
}