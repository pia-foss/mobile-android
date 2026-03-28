package com.kape.featureflags.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.koin.core.annotation.Singleton

@Singleton
class ForceUpdateUseCase(private val featureFlagsDataSource: FeatureFlagsDataSource) {

    fun requiresForceUpdate(): Flow<Boolean> = flow {
        featureFlagsDataSource.invoke().collect { flags ->
            emit(flags.contains("force-update-required"))
        }
    }
}