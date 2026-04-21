package com.kape.dedicatedip.domain

import com.kape.dedicatedip.DataForTest
import com.kape.dedicatedip.utils.DipApiResult
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class RenewDipUseCaseTest : DataForTest() {
    private val dataSource: DipDataSource = mockk()
    private lateinit var useCase: RenewDipUseCase

    @BeforeEach
    fun setUp() {
        useCase = RenewDipUseCase(dataSource)
    }

    @ParameterizedTest(name = "result: {0}")
    @MethodSource("data")
    fun renew(result: DipApiResult) =
        runTest {
            coEvery { dataSource.renew(any()) } returns result
            val actual = useCase.renew("ipToken")
            Assertions.assertEquals(result, actual)
        }
}