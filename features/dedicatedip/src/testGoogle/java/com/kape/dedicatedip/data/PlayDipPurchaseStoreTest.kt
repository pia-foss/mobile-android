package com.kape.dedicatedip.data

import android.content.Context
import com.kape.data.model.DipPurchaseData
import com.kape.dedicatedip.domain.DipCountry
import com.kape.dedicatedip.domain.DipYearlyPlan
import com.kape.dip.data.DedicatedIpSignupPlans
import com.kape.dip.data.DedicatedIpSupportedCountries
import com.kape.localprefs.prefs.DipPrefs
import com.kape.payments.domain.DipSubscriptionBilling
import com.kape.payments.domain.ProductPrice
import com.kape.ui.utils.PriceFormatter
import com.privateinternetaccess.account.AccountRequestError
import com.privateinternetaccess.account.AndroidAccountAPI
import com.privateinternetaccess.account.model.response.AndroidAddonsSubscriptionsInformation
import com.privateinternetaccess.account.model.response.DedicatedIPTokenDetails
import com.privateinternetaccess.account.model.response.DipCountriesResponse
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.invoke
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

internal class PlayDipPurchaseStoreTest {
    private val api = mockk<AndroidAccountAPI>()
    private val dipBilling = mockk<DipSubscriptionBilling>(relaxed = true)
    private val dipPrefs = mockk<DipPrefs>(relaxed = true)
    private val priceFormatter = mockk<PriceFormatter>()
    private val context = mockk<Context> { every { packageName } returns "com.pia" }
    private val store = PlayDipPurchaseStore(api, dipBilling, mockk(), dipPrefs, priceFormatter, context)

    private val countries =
        DipCountriesResponse(
            listOf(
                DipCountriesResponse.DedicatedIpCountriesAvailable(
                    countryCode = "de",
                    name = "Germany",
                    newRegions = emptyList(),
                    regions = listOf("Berlin"),
                ),
            ),
        )
    private val germany = listOf(DipCountry("de", "Germany", listOf("Berlin"), emptyList()))
    private val purchase = DipPurchaseData(token = "token", productId = "dip_yearly", orderId = "order")
    private val error = listOf(AccountRequestError(500, "boom", 0))

    private fun now() = System.currentTimeMillis()

    private fun respondToCountries(
        details: DipCountriesResponse?,
        errors: List<AccountRequestError> = emptyList(),
    ) {
        every { api.supportedDedicatedIPCountries(captureLambda()) } answers {
            lambda<(DipCountriesResponse?, List<AccountRequestError>) -> Unit>().invoke(details, errors)
        }
    }

    private fun respondToAddonSignup(errors: List<AccountRequestError>) {
        every { api.addonSignUp(any(), captureLambda()) } answers {
            lambda<(List<AccountRequestError>) -> Unit>().invoke(errors)
        }
    }

    @Test
    fun `uses countries cached less than 12 hours ago`() =
        runTest {
            coEvery { dipPrefs.getDedicatedIpSupportedCountriesNow() } returns
                DedicatedIpSupportedCountries(now() - 11.hours.inWholeMilliseconds, countries)

            assertEquals(germany, store.supportedCountries())
            verify(exactly = 0) { api.supportedDedicatedIPCountries(any()) }
        }

    @Test
    fun `fetches and stores countries once the cache is older than 12 hours`() =
        runTest {
            coEvery { dipPrefs.getDedicatedIpSupportedCountriesNow() } returns
                DedicatedIpSupportedCountries(now() - 13.hours.inWholeMilliseconds, DipCountriesResponse(emptyList()))
            respondToCountries(countries)

            assertEquals(germany, store.supportedCountries())
            coVerify { dipPrefs.setDedicatedIpSupportedCountries(match { it.supportedCountries == countries }) }
        }

    @Test
    fun `doesn't store anything when fetching countries fails`() =
        runTest {
            coEvery { dipPrefs.getDedicatedIpSupportedCountriesNow() } returns null
            respondToCountries(countries, error)

            assertNull(store.supportedCountries())
            coVerify(exactly = 0) { dipPrefs.setDedicatedIpSupportedCountries(any()) }
        }

    @Test
    fun `the yearly plan matches the plan name in any case and shows a monthly price`() =
        runTest {
            val plans =
                AndroidAddonsSubscriptionsInformation(
                    listOf(
                        AndroidAddonsSubscriptionsInformation.AvailableProduct("dip_monthly", false, "monthly", "9.99"),
                        AndroidAddonsSubscriptionsInformation.AvailableProduct("dip_yearly", false, "Yearly", "99.99"),
                    ),
                    "active",
                )
            coEvery { dipPrefs.getDedicatedIpSignupPlansNow() } returns DedicatedIpSignupPlans(now() - 1.hours.inWholeMilliseconds, plans)
            coEvery { dipBilling.prices(listOf("dip_yearly")) } returns listOf(ProductPrice("dip_yearly", "$99.99", 99_990_000, "USD"))
            every { priceFormatter.formatYearlyPerMonth(99_990_000, "USD", "$99.99") } returns "$8.33/month"

            assertEquals(DipYearlyPlan("dip_yearly", "$99.99", "$8.33/month"), store.yearlyPlan())
        }

    @Test
    fun `stores the purchase before registering it and clears it once registered`() =
        runTest {
            respondToAddonSignup(emptyList())

            assertTrue(store.registerPurchase(purchase).isSuccess)
            coVerifyOrder {
                dipBilling.storePendingPurchase(purchase)
                api.addonSignUp(match { it.receipt.token == "token" && it.receipt.applicationPackage == "com.pia" }, any())
                dipBilling.clearPendingPurchase()
            }
        }

    @Test
    fun `keeps the pending purchase for a retry when registering fails`() =
        runTest {
            respondToAddonSignup(error)

            assertTrue(store.registerPurchase(purchase).isFailure)
            coVerify { dipBilling.storePendingPurchase(purchase) }
            coVerify(exactly = 0) { dipBilling.clearPendingPurchase() }
        }

    @Test
    fun `retries the stored purchase when none is given`() =
        runTest {
            coEvery { dipBilling.pendingPurchase() } returns purchase
            respondToAddonSignup(emptyList())

            assertTrue(store.registerPurchase(null).isSuccess)
            verify { api.addonSignUp(match { it.receipt.orderId == "order" }, any()) }
        }

    @Test
    fun `fails without calling the api when there's no purchase to register`() =
        runTest {
            coEvery { dipBilling.pendingPurchase() } returns null

            assertTrue(store.registerPurchase(null).isFailure)
            verify(exactly = 0) { api.addonSignUp(any(), any()) }
        }

    @Test
    fun `fetching the token fails when the api reports errors`() =
        runTest {
            val details = mockk<DedicatedIPTokenDetails> { every { token } returns "dip-token" }
            every { api.getDedicatedIP("de", "Berlin", captureLambda()) } answers {
                lambda<(DedicatedIPTokenDetails?, List<AccountRequestError>) -> Unit>().invoke(details, error)
            }

            assertTrue(store.fetchToken("de", "Berlin").isFailure)
        }

    @Test
    fun `fetching the token returns it on success`() =
        runTest {
            val details = mockk<DedicatedIPTokenDetails> { every { token } returns "dip-token" }
            every { api.getDedicatedIP("de", "Berlin", captureLambda()) } answers {
                lambda<(DedicatedIPTokenDetails?, List<AccountRequestError>) -> Unit>().invoke(details, emptyList())
            }

            assertEquals("dip-token", store.fetchToken("de", "Berlin").getOrNull())
        }

    // Plans are cached for a day, unlike countries.
    @Test
    fun `refetches plans older than a day`() =
        runTest {
            coEvery { dipPrefs.getDedicatedIpSignupPlansNow() } returns
                DedicatedIpSignupPlans(now() - 2.days.inWholeMilliseconds, AndroidAddonsSubscriptionsInformation(emptyList(), "active"))
            every { api.addonsSubscriptions(captureLambda()) } answers {
                lambda<(AndroidAddonsSubscriptionsInformation?, List<AccountRequestError>) -> Unit>().invoke(null, error)
            }

            assertNull(store.monthlyPlan())
            verify { api.addonsSubscriptions(any()) }
        }
}