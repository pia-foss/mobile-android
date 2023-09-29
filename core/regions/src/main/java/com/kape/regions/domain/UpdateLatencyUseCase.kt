package com.kape.regions.domain

import com.kape.regions.data.RegionRepository
import com.kape.utils.server.Server
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class UpdateLatencyUseCase(private val repo: RegionRepository) {

    fun updateLatencies(): Flow<List<Server>> = flow {
        repo.fetchLatencies().collect {
            emit(it)
        }
    }
}