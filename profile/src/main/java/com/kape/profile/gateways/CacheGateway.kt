package com.kape.profile.gateways

import com.kape.profile.models.Profile
import kotlinx.coroutines.flow.Flow

interface CacheGateway {

    suspend fun getProfile(): Flow<Profile?>

    suspend fun saveProfile(profile: Profile): Result<Unit>

}