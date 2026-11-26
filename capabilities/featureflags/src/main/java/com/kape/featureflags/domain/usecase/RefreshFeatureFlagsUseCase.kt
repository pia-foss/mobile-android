package com.kape.featureflags.domain.usecase

import com.kape.featureflags.domain.FeatureFlagRepository
import org.koin.core.annotation.Factory

@Factory
class RefreshFeatureFlagsUseCase(
    private val repository: FeatureFlagRepository,
) {
    suspend operator fun invoke() = repository.refresh()
}