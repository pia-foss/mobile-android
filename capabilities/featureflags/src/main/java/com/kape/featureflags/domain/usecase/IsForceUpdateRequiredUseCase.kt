package com.kape.featureflags.domain.usecase

import com.kape.featureflags.domain.FeatureFlagRepository
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Factory

private const val FORCE_UPDATE_REQUIRED = "force-update-required"

@Factory
class IsForceUpdateRequiredUseCase(
    private val repository: FeatureFlagRepository,
) {
    suspend operator fun invoke(): Boolean = FORCE_UPDATE_REQUIRED in repository.observeFlags().first()
}