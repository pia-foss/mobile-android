package com.kape.signup.domain

data class Plan(
    val id: String,
    val period: String,
    val hasFreeTrial: Boolean,
    val mainPrice: String,
    val secondaryPrice: String? = null,
    val freeTrialDuration: String?,
    val saving: Int? = null,
)

data class Credentials(
    val status: String,
    val username: String,
    val password: String,
)

sealed interface StoreOffer {
    data object None : StoreOffer

    data object Loading : StoreOffer

    data class Plans(
        val yearly: Plan,
        val monthly: Plan,
    ) : StoreOffer

    data object PlansUnavailable : StoreOffer

    data object WebsiteOnly : StoreOffer

    data object StoreLoginOnly : StoreOffer
}