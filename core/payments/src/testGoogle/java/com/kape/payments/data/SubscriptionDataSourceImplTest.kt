package com.kape.payments.data

import com.kape.payments.domain.SubscriptionDataSource
import com.kape.payments.prefs.SubscriptionPrefs
import com.privateinternetaccess.account.AccountRequestError
import com.privateinternetaccess.account.AndroidAccountAPI
import com.privateinternetaccess.account.model.response.AndroidVpnSubscriptionsInformation
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class SubscriptionDataSourceImplTest {
    private val api: AndroidAccountAPI = mockk()
    private val prefs: SubscriptionPrefs = mockk()

    private val ioScope = CoroutineScope(Dispatchers.Unconfined)

    private val subscriptionsFlow =
        MutableStateFlow<List<Subscription>>(emptyList())

    private lateinit var source: SubscriptionDataSource

    @BeforeEach
    fun setUp() {
        every { prefs.vpnSubscriptions } returns subscriptionsFlow

        source =
            SubscriptionDataSourceImpl(
                prefs = prefs,
                api = api,
                ioScope = ioScope,
            )
    }

    @Test
    fun `returns empty list when api returns error`() =
        runTest {
            coEvery { prefs.storeVpnSubscriptions(any()) } returns Unit

            coEvery { api.vpnSubscriptions(any()) } answers {
                val callback =
                    firstArg<(AndroidVpnSubscriptionsInformation?, List<AccountRequestError>) -> Unit>()
                callback.invoke(null, listOf(AccountRequestError(500, "error")))
            }

            val result = source.getAvailableVpnSubscriptions()

            assertEquals(emptyList(), result)
        }

    @Test
    fun `returns subscriptions when api succeeds`() =
        runTest {
            val apiResponse =
                AndroidVpnSubscriptionsInformation(
                    availableProducts =
                        listOf(
                            AndroidVpnSubscriptionsInformation.AvailableProduct(
                                id = "id",
                                legacy = false,
                                plan = "monthly",
                                price = "3.99",
                            ),
                        ),
                    status = "ok",
                )

            val expected =
                listOf(
                    Subscription("id", false, "monthly", "3.99", null),
                )

            // IMPORTANT: this is what Flow.first { it.isNotEmpty() } will return
            subscriptionsFlow.value = expected

            coEvery { prefs.storeVpnSubscriptions(any()) } returns Unit

            coEvery { api.vpnSubscriptions(any()) } answers {
                val callback =
                    firstArg<(AndroidVpnSubscriptionsInformation?, List<AccountRequestError>) -> Unit>()
                callback.invoke(apiResponse, emptyList())
            }

            val result = source.getAvailableVpnSubscriptions()

            assertEquals(expected, result)
        }
}