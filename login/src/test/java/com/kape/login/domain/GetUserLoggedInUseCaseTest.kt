package com.kape.login.domain

import com.kape.login.BaseTest
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
internal class GetUserLoggedInUseCaseTest : BaseTest() {

    private val source = mockk<AuthenticationDataSource>()

    private lateinit var useCase: GetUserLoggedInUseCase

    @BeforeEach
    internal fun setUp() {
        useCase = GetUserLoggedInUseCase(source)
    }

    @ParameterizedTest(name = "expected: {0}")
    @MethodSource("booleans")
    fun login(expected: Boolean) = runTest {
        every { source.isUserLoggedIn() } returns expected
        val actual = useCase.isUserLoggedIn()
        assertEquals(expected, actual)
    }
}