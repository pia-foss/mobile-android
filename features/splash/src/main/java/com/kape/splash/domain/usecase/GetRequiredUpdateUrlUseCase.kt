package com.kape.splash.domain.usecase

import com.kape.contracts.di.Qualifiers
import com.kape.featureflags.domain.usecase.IsForceUpdateRequiredUseCase
import com.kape.splash.domain.AppUpdateRepository
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Named

@Factory
class GetRequiredUpdateUrlUseCase(
    private val isForceUpdateRequired: IsForceUpdateRequiredUseCase,
    private val repository: AppUpdateRepository,
    @Named(Qualifiers.UPDATE_URL) private val storeUpdateUrl: String,
) {
    suspend operator fun invoke(): String? {
        if (!isForceUpdateRequired()) return null
        val latestReleaseUrl = repository.getLatestRelease()?.url ?: return null
        return storeUpdateUrl.ifEmpty { latestReleaseUrl }
    }
}