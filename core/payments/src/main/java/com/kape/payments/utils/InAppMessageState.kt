package com.kape.payments.utils

sealed class InAppMessageState {
    data object Default : InAppMessageState()

    data object NoActionNeeded : InAppMessageState()

    data class SubscriptionStatusUpdated(
        val purchaseToken: String,
    ) : InAppMessageState()
}