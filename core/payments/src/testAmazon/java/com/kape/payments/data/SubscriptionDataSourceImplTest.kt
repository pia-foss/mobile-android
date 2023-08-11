package com.kape.payments.data

import app.cash.turbine.test
import com.kape.payments.di.paymentsModule
import com.kape.payments.domain.SubscriptionDataSource
import com.kape.payments.data.models.Subscription
import com.kape.payments.utils.SubscriptionPrefs
import com.privateinternetaccess.account.AccountRequestError
import com.privateinternetaccess.account.AndroidAccountAPI
import com.privateinternetaccess.account.model.response.AmazonSubscriptionsInformation
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import java.util.stream.Stream
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
internal class SubscriptionDataSourceImplTest {

    private val api: AndroidAccountAPI = mockk(relaxed = true)
    private val prefs: SubscriptionPrefs = mockk(relaxed = true)

    private lateinit var source: SubscriptionDataSource

    private val appModule = module {
        single { api }
    }

    @OptIn(DelicateCoroutinesApi::class)
    val mainThreadSurrogate = newSingleThreadContext("UI thread")

    @BeforeEach
    internal fun setUp() {
        stopKoin()
        startKoin {
            modules(appModule, paymentsModule)
        }
        source = SubscriptionDataSourceImpl(prefs)
    }

    @AfterEach
    internal fun tearDown() {
        Dispatchers.resetMain() // reset the main dispatcher to the original Main dispatcher
        mainThreadSurrogate.close()
    }

    @ParameterizedTest(name = "api: {0}")
    @MethodSource("accountApiResults")
    fun `getAvailableSubscriptions() - unsuccessful`(errorList: List<AccountRequestError>) = runTest {

        coEvery { api.amazonSubscriptions(any()) } answers {
            lastArg<(AmazonSubscriptionsInformation?, List<AccountRequestError>) -> Unit>().invoke(null, errorList)
        }

        source.getAvailableSubscriptions().test {
            val actual = awaitItem()
            assertEquals(emptyList(), actual)
        }
    }

    @Test
    fun `getAvailableSubscriptions - successful`() = runTest {
        val data =
            AmazonSubscriptionsInformation(listOf(AmazonSubscriptionsInformation.AvailableProduct("id", false, "monthly", 3.99)), "ok")
        val expected = listOf(Subscription("id", false, "monthly", 3.99))
        coEvery { api.amazonSubscriptions(any()) } answers {
            lastArg<(AmazonSubscriptionsInformation?, List<AccountRequestError>) -> Unit>().invoke(data, emptyList())
        }
        every { prefs.storeSubscriptions(any()) } returns Unit
        every { prefs.getSubscriptions() } returns expected

        source.getAvailableSubscriptions().test {
            val actual = awaitItem()
            assertEquals(expected, actual)
        }
    }

    companion object {
        @JvmStatic
        fun accountApiResults() = Stream.of(
            Arguments.of(listOf(AccountRequestError(code = 600, message = null))),
            Arguments.of(listOf(AccountRequestError(code = 429, message = null))),
            Arguments.of(listOf(AccountRequestError(code = 401, message = null))),
            Arguments.of(listOf(AccountRequestError(code = 402, message = null)))
        )
    }
}