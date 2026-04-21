package com.kape.profile.domain

import org.koin.core.annotation.Singleton

@Singleton
class DeleteAccountUseCase(
    private val profile: ProfileDatasource,
) {
    suspend fun deleteAccount(): Boolean = profile.deleteAccount()
}