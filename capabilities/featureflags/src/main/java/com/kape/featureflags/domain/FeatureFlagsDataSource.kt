package com.kape.featureflags.domain

import kotlinx.coroutines.flow.Flow

fun interface FeatureFlagsDataSource {
    operator fun invoke(): Flow<List<String>>
}