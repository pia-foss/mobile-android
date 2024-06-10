package com.kape.payments.domain

import com.kape.payments.SubscriptionPrefs
import com.kape.payments.data.PurchaseData
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.test.assertEquals

class GetPurchaseDetailsUseCaseTest {
    private val prefs: SubscriptionPrefs = mockk()
    private lateinit var useCase: GetPurchaseDetailsUseCase

    @BeforeEach
    fun setUp() {
        useCase = GetPurchaseDetailsUseCase(prefs)
    }

    @ParameterizedTest(name = "expected: {0}")
    @MethodSource("prefsValues")
    fun `test getPurchaseDetails`(expected: PurchaseData?) = runTest {
        every { prefs.getVpnPurchaseData() } returns expected

        val result = useCase.getPurchaseDetails()
        assertEquals(expected, result)
    }

    companion object {
        @JvmStatic
        fun prefsValues() = Stream.of(
            Arguments.of(null),
            Arguments.of(PurchaseData("token", "productId", "orderId")),
        )
    }
}