package com.kape.profile.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.koin.core.annotation.Singleton

@Singleton
class DeleteAccountUseCase(private val profile: ProfileDatasource) {

    fun deleteAccount(): Flow<Boolean> = flow {
        profile.deleteAccount().collect {
            emit(it)
        }
    }
}