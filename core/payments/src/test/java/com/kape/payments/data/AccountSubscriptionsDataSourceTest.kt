package com.kape.payments.data

import com.kape.data.model.Subscription
import com.privateinternetaccess.account.AccountRequestError
import com.privateinternetaccess.account.AndroidAccountAPI
import com.privateinternetaccess.account.model.response.AndroidVpnSubscriptionsInformation
import io.mockk.every
import io.mockk.invoke
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

internal class AccountSubscriptionsDataSourceTest {
    private val api = mockk<AndroidAccountAPI>()
    private val dataSource = AccountSubscriptionsDataSource(api)

    private fun respond(
        details: AndroidVpnSubscriptionsInformation?,
        errors: List<AccountRequestError> = emptyList(),
    ) {
        every { api.vpnSubscriptions(captureLambda()) } answers {
            lambda<(AndroidVpnSubscriptionsInformation?, List<AccountRequestError>) -> Unit>().invoke(details, errors)
        }
    }

    private fun product(
        id: String,
        legacy: Boolean = false,
    ) = AndroidVpnSubscriptionsInformation.AvailableProduct(id = id, legacy = legacy, plan = "yearly", price = "59.99")

    @Test
    fun `maps every available product, legacy ones included`() =
        runTest {
            respond(AndroidVpnSubscriptionsInformation(listOf(product("a"), product("b", legacy = true)), "active"))

            assertEquals(
                listOf(
                    Subscription("a", false, "yearly", "59.99", null),
                    Subscription("b", true, "yearly", "59.99", null),
                ),
                dataSource.fetchSubscriptions(),
            )
        }

    @Test
    fun `fails when the api reports errors, even with details`() =
        runTest {
            respond(AndroidVpnSubscriptionsInformation(listOf(product("a")), "active"), listOf(AccountRequestError(500, "boom")))

            assertNull(dataSource.fetchSubscriptions())
        }

    @Test
    fun `fails without details`() =
        runTest {
            respond(null)

            assertNull(dataSource.fetchSubscriptions())
        }
}