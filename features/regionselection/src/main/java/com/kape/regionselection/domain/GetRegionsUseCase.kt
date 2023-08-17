package com.kape.regionselection.domain

import com.kape.regionselection.data.RegionRepository
import com.kape.regionselection.utils.RegionPrefs
import com.kape.utils.server.Server
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetRegionsUseCase(private val repo: RegionRepository, private val prefs: RegionPrefs) {

    fun loadRegions(locale: String): Flow<List<Server>> = flow {
        repo.fetchRegions(locale).collect {
            emit(it)
        }
    }

    fun getFavoriteServers(): List<String> = prefs.getFavoriteServers()

    fun getSelectedRegion() = prefs.getSelectedServerKey()

    fun selectRegion(key: String) = prefs.selectServer(key)
}