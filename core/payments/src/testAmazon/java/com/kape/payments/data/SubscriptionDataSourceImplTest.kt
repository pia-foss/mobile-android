package com.kape.payments.data

import com.kape.payments.SubscriptionPrefs
import com.kape.payments.domain.SubscriptionDataSource
import com.privateinternetaccess.account.AccountRequestError
import com.privateinternetaccess.account.AndroidAccountAPI
import com.privateinternetaccess.account.model.response.AmazonSubscriptionsInformation
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import java.util.stream.Stream
import kotlin.test.assertEquals

internal class SubscriptionDataSourceImplTest {

    private val api: AndroidAccountAPI = mockk(relaxed = true)
    private val prefs: SubscriptionPrefs = mockk(relaxed = true)

    private lateinit var source: SubscriptionDataSource

    @BeforeEach
    internal fun setUp() {
        stopKoin()
        startKoin {}
        source = SubscriptionDataSourceImpl(prefs, api)
    }

    @ParameterizedTest(name = "api: {0}")
    @MethodSource("accountApiResults")
    fun `getAvailableSubscriptions() - unsuccessful`(errorList: List<AccountRequestError>) = runTest {
        coEvery { api.amazonSubscriptions(any()) } answers {
            lastArg<(AmazonSubscriptionsInformation?, List<AccountRequestError>) -> Unit>()
                .invoke(null, errorList)
        }
        val actual = source.getAvailableVpnSubscriptions()
        assertEquals(emptyList(), actual)
    }

    @Test
    fun `getAvailableSubscriptions - successful`() = runTest {
        val data = AmazonSubscriptionsInformation(
            listOf(AmazonSubscriptionsInformation.AvailableProduct("id", false, "monthly", 3.99)),
            "ok",
        )
        val expected = listOf(Subscription("id", false, "monthly", 3.99))
        coEvery { api.amazonSubscriptions(any()) } answers {
            lastArg<(AmazonSubscriptionsInformation?, List<AccountRequestError>) -> Unit>()
                .invoke(data, emptyList())
        }
        every { prefs.storeVpnSubscriptions(any()) } returns Unit
        every { prefs.getVpnSubscriptions() } returns expected

        val actual = source.getAvailableVpnSubscriptions()
        assertEquals(expected, actual)
    }

    companion object {
        @JvmStatic
        fun accountApiResults() = Stream.of(
            Arguments.of(listOf(AccountRequestError(code = 600, message = null))),
            Arguments.of(listOf(AccountRequestError(code = 429, message = null))),
            Arguments.of(listOf(AccountRequestError(code = 401, message = null))),
            Arguments.of(listOf(AccountRequestError(code = 402, message = null))),
        )
    }
}