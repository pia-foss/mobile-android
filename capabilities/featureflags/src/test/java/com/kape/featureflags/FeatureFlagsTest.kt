package com.kape.featureflags

import com.kape.featureflags.data.FeatureFlagLocalDataSource
import com.kape.featureflags.data.FeatureFlagRepositoryImpl
import com.kape.featureflags.domain.usecase.IsForceUpdateRequiredUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class FeatureFlagsTest {
    private val local = FakeLocal()
    private var remoteFlags: List<String>? = null
    private val repository = FeatureFlagRepositoryImpl({ remoteFlags }, local)
    private val isForceUpdateRequired = IsForceUpdateRequiredUseCase(repository)

    @Test
    fun `stores the fetched flags`() =
        runTest {
            remoteFlags = listOf("a", "b")

            repository.refresh()

            assertEquals(listOf("a", "b"), local.flags.value)
        }

    @Test
    fun `keeps the stored flags when fetching fails`() =
        runTest {
            local.flags.value = listOf("stored")
            remoteFlags = null

            repository.refresh()

            assertEquals(listOf("stored"), local.flags.value)
        }

    @Test
    fun `requires a force update only when the flag is set`() =
        runTest {
            assertFalse(isForceUpdateRequired())

            local.flags.value = listOf("force-update-required")

            assertTrue(isForceUpdateRequired())
        }
}

private class FakeLocal : FeatureFlagLocalDataSource {
    val flags = MutableStateFlow<List<String>>(emptyList())

    override fun observeFlags() = flags

    override suspend fun saveFlags(flags: List<String>) {
        this.flags.value = flags
    }
}