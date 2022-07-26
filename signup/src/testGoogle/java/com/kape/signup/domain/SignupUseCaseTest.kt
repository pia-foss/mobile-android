package com.kape.signup.domain

import app.cash.turbine.test
import com.kape.payments.domain.GetPurchaseDetailsUseCase
import com.kape.payments.models.PurchaseData
import com.kape.signup.models.Credentials
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.koin.test.KoinTest
import java.util.stream.Stream
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
internal class SignupUseCaseTest : KoinTest {

    private val signupDataSource: SignupDataSource = mockk()
    private val purchaseDetailsUseCase: GetPurchaseDetailsUseCase = mockk()

    private lateinit var useCase: SignupUseCase

    @BeforeEach
    fun setUp() {
        useCase = SignupUseCase(signupDataSource, purchaseDetailsUseCase)
    }

    @ParameterizedTest(name = "expected: {0}, data: {1}")
    @MethodSource("arguments")
    fun `test signup`(expected: Credentials?, purchaseData: PurchaseData?) = runTest {
        every { purchaseDetailsUseCase.getPurchaseDetails() } returns purchaseData
        coEvery { signupDataSource.signup(any(), any(), any()) } returns flow {
            emit(expected)
        }

        useCase.signup().test {
            val actual = awaitItem()
            awaitComplete()
            assertEquals(expected, actual)
        }
    }

    companion object {

        val credentials = Credentials("status", "username", "password")
        val nullCredentials: Credentials? = null
        val purchaseData = PurchaseData("token", "productId", "orderId")
        val missingPurchaseData: PurchaseData? = null


        @JvmStatic
        fun arguments() = Stream.of(Arguments.of(credentials, purchaseData), Arguments.of(nullCredentials, missingPurchaseData))
    }
}