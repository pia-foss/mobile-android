package com.kape.profile.domain

import com.kape.profile.data.models.Profile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.koin.core.annotation.Singleton

@Singleton
class GetProfileUseCase(private val profile: ProfileDatasource) {

    fun getProfile(): Flow<Profile?> = flow {
        profile.accountDetails().collect { profile ->
            emit(profile)
        }
    }
}