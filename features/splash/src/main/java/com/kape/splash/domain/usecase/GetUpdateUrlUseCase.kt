package com.kape.splash.domain.usecase

import com.kape.contracts.di.Qualifiers
import com.kape.splash.domain.AppUpdateRepository
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Named

@Factory
class GetUpdateUrlUseCase(
    private val repository: AppUpdateRepository,
    @Named(Qualifiers.UPDATE_URL) private val storeUpdateUrl: String,
) {
    suspend operator fun invoke(): String? = storeUpdateUrl.ifEmpty { null } ?: repository.getLatestRelease()?.url
}