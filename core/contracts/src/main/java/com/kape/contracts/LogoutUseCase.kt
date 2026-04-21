package com.kape.contracts

interface LogoutUseCase {
    suspend fun logout(): Boolean
}