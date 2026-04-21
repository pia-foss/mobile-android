package com.kape.signup.domain

interface EmailDataSource {
    suspend fun setEmail(email: String): Boolean
}