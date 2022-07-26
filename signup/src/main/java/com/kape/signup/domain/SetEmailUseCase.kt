package com.kape.signup.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SetEmailUseCase(private val source: EmailDataSource) {

    suspend fun setEmail(email: String): Flow<Boolean> = flow {
        source.setEmail(email).collect {
            emit(it)
        }
    }
}