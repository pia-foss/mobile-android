package com.kape.profile.domain

import com.kape.profile.models.Profile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetProfileUseCase(private val profile: ProfileDatasource) {

    suspend fun getProfile(): Flow<Profile?> = flow {
        profile.accountDetails().collect { profile ->
            emit(profile)
        }
    }
}