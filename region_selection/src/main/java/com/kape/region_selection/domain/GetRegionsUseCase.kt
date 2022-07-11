package com.kape.region_selection.domain

import com.kape.region_selection.data.RegionRepository
import com.kape.region_selection.server.Server
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetRegionsUseCase(private val repo: RegionRepository) {

    suspend fun loadRegions(locale: String): Flow<List<Server>> = flow {
        repo.fetchRegions(locale).collect {
            emit(it)
        }
    }
}