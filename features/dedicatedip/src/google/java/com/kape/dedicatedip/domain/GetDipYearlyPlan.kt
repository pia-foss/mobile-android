package com.kape.dedicatedip.domain

import com.kape.dedicatedip.data.models.DedicatedIpYearlyPlan
import com.kape.payments.utils.YEARLY_SUBSCRIPTION
import com.kape.ui.utils.PriceFormatter
import org.koin.core.annotation.Singleton

@Singleton
class GetDipYearlyPlan(
    private val getDipProductDetails: GetDipProductDetails,
    private val formatter: PriceFormatter,
) {
    suspend operator fun invoke(): DedicatedIpYearlyPlan? {
        val (id, price) = getDipProductDetails(YEARLY_SUBSCRIPTION) ?: return null
        return DedicatedIpYearlyPlan(
            id = id,
            yearlyPrice = price,
            // 0L added to match formatYearlyPerMonth signature. To be fixed when dip goes live.
            monthlyPrice =
                formatter.formatYearlyPerMonth(
                    priceInMicros = 0L,
                    currencyCode = "USD",
                    originalFormattedPrice = price,
                ),
        )
    }
}