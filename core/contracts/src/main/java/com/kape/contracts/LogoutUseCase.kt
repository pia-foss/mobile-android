package com.kape.contracts

import kotlinx.coroutines.flow.Flow

interface LogoutUseCase {
    fun logout(): Flow<Boolean>
}