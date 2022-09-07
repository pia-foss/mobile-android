package com.kape.region_selection.domain

import com.kape.core.server.Server
import com.kape.region_selection.data.RegionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class UpdateLatencyUseCase(private val repo: RegionRepository) {

    suspend fun updateLatencies(): Flow<List<Server>> = flow {
        repo.fetchLatencies().collect {
            emit(it)
        }
    }
}