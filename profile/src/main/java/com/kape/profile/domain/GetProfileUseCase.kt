package com.kape.profile.domain

import com.kape.profile.models.Profile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetProfileUseCase(private val profile: ProfileDatasource) {

    suspend fun getProfile(): Flow<Profile?> = flow {
        try {
            profile.accountDetails().collect { profile ->
                emit(profile)
            }
        } catch (t: Throwable) {
            emit(null)
        }
    }
}