package com.kape.payments.domain

import com.kape.payments.data.Subscription
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class GetSubscriptionsUseCaseTest {
    private val source: SubscriptionDataSource = mockk(relaxed = true)

    private lateinit var useCase: GetSubscriptionsUseCase

    @BeforeEach
    internal fun setUp() {
        useCase = GetSubscriptionsUseCase(source)
    }

    @Test
    fun `getSubscriptions() - success`() = runTest {
        val expected = listOf(
            Subscription("id", false, "monthly", "3.99", "$ 3.99"),
            Subscription("id", false, "yearly", "49.99", "$ 49.99"),
        )
        coEvery { source.getAvailableVpnSubscriptions() } returns expected
        val actual = useCase.getVpnSubscriptions()
        assertEquals(expected, actual)
    }

    @Test
    fun `getSubscriptions() - failed`() = runTest {
        coEvery { source.getAvailableVpnSubscriptions() } returns emptyList()
        val actual = useCase.getVpnSubscriptions()
        assertEquals(emptyList(), actual)
    }
}