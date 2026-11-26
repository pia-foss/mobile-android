package com.kape.dedicatedip.domain

sealed interface DipActivationResult {
    data object Active : DipActivationResult

    data object Invalid : DipActivationResult

    data object Expired : DipActivationResult

    data object Error : DipActivationResult
}

data class DipCountry(
    val countryCode: String,
    val name: String,
    val regions: List<String>,
    val newRegions: List<String>,
)

data class DipMonthlyPlan(
    val id: String,
    val monthlyPrice: String,
)

data class DipYearlyPlan(
    val id: String,
    val yearlyPrice: String,
    val monthlyPrice: String,
)