package com.kape.featureflags.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class FeatureFlagRepositoryImplTest {
    private val stored = MutableStateFlow(listOf("stored"))
    private var saves = 0
    private val local =
        object : FeatureFlagLocalDataSource {
            override fun observeFlags() = stored

            override suspend fun saveFlags(flags: List<String>) {
                saves++
                stored.value = flags
            }
        }

    @Test
    fun `observes the stored flags`() =
        runTest {
            assertEquals(listOf("stored"), FeatureFlagRepositoryImpl({ null }, local).observeFlags().first())
        }

    @Test
    fun `an empty fetch clears the stored flags`() =
        runTest {
            FeatureFlagRepositoryImpl({ emptyList() }, local).refresh()

            assertEquals(emptyList<String>(), stored.value)
        }

    @Test
    fun `a failed fetch doesn't touch storage`() =
        runTest {
            FeatureFlagRepositoryImpl({ null }, local).refresh()

            assertEquals(0, saves)
        }
}