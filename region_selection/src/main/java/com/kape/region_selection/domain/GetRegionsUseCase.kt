package com.kape.region_selection.domain

import com.kape.region_selection.data.RegionRepository
import com.kape.region_selection.server.Server
import com.kape.region_selection.utils.RegionPrefs
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetRegionsUseCase(private val repo: RegionRepository, private val prefs: RegionPrefs) {

    suspend fun loadRegions(locale: String): Flow<List<Server>> = flow {
        repo.fetchRegions(locale).collect {
            emit(it)
        }
    }

    fun getSelectedRegion() = prefs.getSelectedServerKey()

    fun selectRegion(key: String) = prefs.selectServer(key)
}