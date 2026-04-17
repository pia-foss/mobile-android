package com.kape.signup.domain

import org.koin.core.annotation.Singleton

@Singleton
class SetEmailUseCase(private val source: EmailDataSource) {

    suspend fun setEmail(email: String): Boolean = source.setEmail(email)
}