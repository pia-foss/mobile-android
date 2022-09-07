package com.kape.payments.domain

import app.cash.turbine.test
import com.kape.payments.models.Subscription
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
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
            Subscription("id", false, "yearly", "49.99", "$ 49.99")
        )
        every { source.getAvailableSubscriptions() } returns flow {
            emit(expected)
        }
        useCase.getSubscriptions().test {
            val actual = awaitItem()
            awaitComplete()
            kotlin.test.assertEquals(expected, actual)
        }
    }

    @Test
    fun `getSubscriptions() - failed`() = runTest {
        val expected = emptyList<Subscription>()
        every { source.getAvailableSubscriptions() } returns flow {
            emit(expected)
        }
        useCase.getSubscriptions().test {
            val actual = awaitItem()
            awaitComplete()
            kotlin.test.assertEquals(expected, actual)
        }
    }
}
