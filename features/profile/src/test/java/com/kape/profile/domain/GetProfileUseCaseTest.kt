package com.kape.profile.domain

import com.kape.profile.data.models.Profile
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.test.assertEquals

class GetProfileUseCaseTest {

    private val dataSource: ProfileDatasource = mockk()

    lateinit var useCase: GetProfileUseCase

    @BeforeEach
    fun setUp() {
        useCase = GetProfileUseCase(dataSource)
    }

    @ParameterizedTest(name = "result: {0}, expected: {1}")
    @MethodSource("data")
    fun getProfile(result: Profile?, expected: Profile?) = runTest {
        coEvery { dataSource.accountDetails() } returns result
        val actual = useCase.getProfile()
        assertEquals(expected, actual)
    }

    companion object {
        private val result = Profile(username = "username", subscription = mockk())

        @JvmStatic
        fun data() = Stream.of(
            Arguments.of(null, null),
            Arguments.of(result, result),
        )
    }
}