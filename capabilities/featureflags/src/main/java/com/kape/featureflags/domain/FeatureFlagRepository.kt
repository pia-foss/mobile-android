package com.kape.featureflags.domain

import kotlinx.coroutines.flow.Flow

interface FeatureFlagRepository {
    fun observeFlags(): Flow<List<String>>

    suspend fun refresh()
}