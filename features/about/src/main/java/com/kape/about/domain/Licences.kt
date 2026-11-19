package com.kape.about.domain

import org.koin.core.annotation.Factory

fun interface LicenceRepository {
    suspend fun readLicences(): List<String>
}

@Factory
class GetLicencesUseCase(
    private val repository: LicenceRepository,
) {
    suspend operator fun invoke(): List<String> = repository.readLicences()
}