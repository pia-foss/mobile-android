package com.kape.dedicatedip.data

import android.app.Activity
import android.content.Context
import com.kape.data.model.DipPurchaseData
import com.kape.dedicatedip.domain.DipCountry
import com.kape.dedicatedip.domain.DipMonthlyPlan
import com.kape.dedicatedip.domain.DipPurchaseStore
import com.kape.dedicatedip.domain.DipYearlyPlan
import com.kape.dip.data.DedicatedIpSignupPlans
import com.kape.dip.data.DedicatedIpSupportedCountries
import com.kape.localprefs.prefs.DipPrefs
import com.kape.payments.domain.DipSubscriptionBilling
import com.kape.payments.domain.ProductPrice
import com.kape.payments.domain.VpnSubscriptionBilling
import com.kape.ui.utils.PriceFormatter
import com.privateinternetaccess.account.AndroidAccountAPI
import com.privateinternetaccess.account.model.request.AndroidAddonSignupInformation
import com.privateinternetaccess.account.model.response.AndroidAddonsSubscriptionsInformation
import com.privateinternetaccess.account.model.response.DipCountriesResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.core.annotation.Singleton
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

private const val RECEIPT_STORE = "google_play"
private const val MONTHLY_PLAN = "monthly"
private const val YEARLY_PLAN = "yearly"
private val PLANS_CACHE_DURATION = 1.days
private val COUNTRIES_CACHE_DURATION = 12.hours

@Singleton(binds = [DipPurchaseStore::class])
internal class PlayDipPurchaseStore(
    private val api: AndroidAccountAPI,
    private val dipBilling: DipSubscriptionBilling,
    private val vpnBilling: VpnSubscriptionBilling,
    private val dipPrefs: DipPrefs,
    private val priceFormatter: PriceFormatter,
    private val context: Context,
) : DipPurchaseStore {
    override suspend fun supportedCountries(): List<DipCountry>? {
        val cached = dipPrefs.getDedicatedIpSupportedCountriesNow()
        val response =
            if (cached != null && !cached.persistedTimestamp.isOlderThan(COUNTRIES_CACHE_DURATION.inWholeMilliseconds)) {
                cached.supportedCountries
            } else {
                fetchSupportedCountries()?.also {
                    dipPrefs.setDedicatedIpSupportedCountries(DedicatedIpSupportedCountries(System.currentTimeMillis(), it))
                }
            }
        return response?.dedicatedIpCountriesAvailable?.map { DipCountry(it.countryCode, it.name, it.regions, it.newRegions) }
    }

    override suspend fun monthlyPlan(): DipMonthlyPlan? = price(MONTHLY_PLAN)?.let { DipMonthlyPlan(it.productId, it.formattedPrice) }

    override suspend fun yearlyPlan(): DipYearlyPlan? =
        price(YEARLY_PLAN)?.let {
            DipYearlyPlan(
                id = it.productId,
                yearlyPrice = it.formattedPrice,
                monthlyPrice = priceFormatter.formatYearlyPerMonth(it.priceInMicros, it.currencyCode, it.formattedPrice),
            )
        }

    override suspend fun purchase(
        activity: Activity,
        productId: String,
    ): Result<DipPurchaseData> = dipBilling.purchase(activity, productId)

    override suspend fun unregisteredPurchases(productIds: List<String>): List<String>? = dipBilling.unacknowledgedProducts(productIds)

    override suspend fun registerPurchase(purchase: DipPurchaseData?): Result<Unit> {
        val toRegister = purchase ?: dipBilling.pendingPurchase() ?: return Result.failure(IllegalStateException("Unknown purchase data"))
        // Stored first, so registering can be retried if it fails or the app closes.
        dipBilling.storePendingPurchase(toRegister)
        val registered =
            suspendCancellableCoroutine { continuation ->
                val information =
                    AndroidAddonSignupInformation(
                        receipt =
                            AndroidAddonSignupInformation.Receipt(
                                applicationPackage = context.packageName,
                                productId = toRegister.productId,
                                orderId = toRegister.orderId,
                                token = toRegister.token,
                            ),
                        store = RECEIPT_STORE,
                    )
                api.addonSignUp(information) { errors -> continuation.resume(errors.isEmpty()) }
            }
        if (!registered) return Result.failure(IllegalStateException("Signup validation failed"))
        dipBilling.clearPendingPurchase()
        return Result.success(Unit)
    }

    override suspend fun fetchToken(
        countryCode: String,
        regionName: String,
    ): Result<String> =
        suspendCancellableCoroutine { continuation ->
            api.getDedicatedIP(countryCode, regionName) { details, errors ->
                continuation.resume(
                    if (details == null || errors.isNotEmpty()) {
                        Result.failure(IllegalStateException("Fetch token failed"))
                    } else {
                        Result.success(details.token)
                    },
                )
            }
        }

    override fun hasActiveVpnSubscription(): Flow<Boolean> = vpnBilling.hasActiveSubscription()

    override suspend fun renew(dipToken: String): Boolean =
        suspendCancellableCoroutine { continuation ->
            api.renewDedicatedIP(dipToken) { errors -> continuation.resume(errors.isEmpty()) }
        }

    private suspend fun price(plan: String): ProductPrice? {
        val product = signupPlans()?.availableProducts?.firstOrNull { it.plan.equals(plan, ignoreCase = true) } ?: return null
        return dipBilling.prices(listOf(product.id))?.firstOrNull()
    }

    private suspend fun signupPlans(): AndroidAddonsSubscriptionsInformation? {
        val cached = dipPrefs.getDedicatedIpSignupPlansNow()
        if (cached != null && !cached.persistedTimestamp.isOlderThan(PLANS_CACHE_DURATION.inWholeMilliseconds)) return cached.signupPlans
        return fetchSignupPlans()?.also { dipPrefs.setDedicatedIpSignupPlans(DedicatedIpSignupPlans(System.currentTimeMillis(), it)) }
    }

    private suspend fun fetchSignupPlans(): AndroidAddonsSubscriptionsInformation? =
        suspendCancellableCoroutine { continuation ->
            api.addonsSubscriptions { details, errors -> continuation.resume(if (errors.isNotEmpty()) null else details) }
        }

    private suspend fun fetchSupportedCountries(): DipCountriesResponse? =
        suspendCancellableCoroutine { continuation ->
            api.supportedDedicatedIPCountries { details, errors -> continuation.resume(if (errors.isNotEmpty()) null else details) }
        }

    private fun Long.isOlderThan(millis: Long) = System.currentTimeMillis() - this > millis
}