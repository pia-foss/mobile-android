package com.kape.featureflags.domain

import org.koin.core.annotation.Singleton

@Singleton
class ForceUpdateUseCase(private val featureFlagsDataSource: FeatureFlagsDataSource) {

    suspend fun requiresForceUpdate(): Boolean =
        featureFlagsDataSource.invoke().contains("force-update-required")
}