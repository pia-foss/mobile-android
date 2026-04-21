package com.kape.profile.domain

import com.kape.profile.data.models.Profile
import org.koin.core.annotation.Singleton

@Singleton
class GetProfileUseCase(
    private val profile: ProfileDatasource,
) {
    suspend fun getProfile(): Profile? = profile.accountDetails()
}