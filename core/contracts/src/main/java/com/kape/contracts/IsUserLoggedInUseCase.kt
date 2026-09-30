package com.kape.contracts

interface IsUserLoggedInUseCase {
    suspend fun invoke(): Boolean
}