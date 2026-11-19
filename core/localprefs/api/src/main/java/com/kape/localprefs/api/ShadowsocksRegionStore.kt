package com.kape.localprefs.api

import kotlinx.coroutines.flow.Flow

interface ShadowsocksRegionStore {
    suspend fun addToFavorites(shadowsocksServerName: String)

    fun isFavorite(shadowsocksServerName: String): Flow<Boolean>

    fun observeFavorites(): Flow<List<String>>

    suspend fun removeFromFavorites(shadowsocksServerName: String)
}