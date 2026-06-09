package com.kape.signup.domain

import com.kape.signup.data.models.Credentials

interface SignupHandler {
    suspend fun vpnSignup(email: String): Credentials?
}