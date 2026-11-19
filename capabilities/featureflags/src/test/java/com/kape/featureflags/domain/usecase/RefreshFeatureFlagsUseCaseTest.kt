package com.kape.featureflags.domain.usecase

import com.kape.featureflags.domain.FeatureFlagRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class RefreshFeatureFlagsUseCaseTest {
    @Test
    fun `refreshes the repository`() =
        runTest {
            val repository = CountingRepository()

            RefreshFeatureFlagsUseCase(repository)()

            assertEquals(1, repository.refreshes)
        }
}

private class CountingRepository : FeatureFlagRepository {
    var refreshes = 0

    override fun observeFlags(): Flow<List<String>> = emptyFlow()

    override suspend fun refresh() {
        refreshes++
    }
}