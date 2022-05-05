package com.kape.profile.domain

import com.kape.profile.data.NetworkDatasource
import com.kape.profile.gateways.CacheGateway
import com.kape.profile.models.Profile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetProfileUseCase(private val cache: CacheGateway, private val network: NetworkDatasource) {

    suspend fun getProfile(): Flow<Profile> = flow {
        cache.getProfile().collect { cachedProfile ->
            if (cachedProfile != null) {
                emit(cachedProfile)
            } else {
                network.accountDetails().collect { profile ->
                    emit(profile)
                }
            }
        }
    }
}