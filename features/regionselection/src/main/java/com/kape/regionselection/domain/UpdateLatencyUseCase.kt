package com.kape.regionselection.domain

import com.kape.regionselection.data.RegionRepository
import com.kape.utils.server.Server
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class UpdateLatencyUseCase(private val repo: RegionRepository) {

    suspend fun updateLatencies(): Flow<List<Server>> = flow {
        repo.fetchLatencies().collect {
            emit(it)
        }
    }
}