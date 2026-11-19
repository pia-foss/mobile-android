package com.kape.signup.data

import android.app.Activity
import com.kape.contracts.SubscriptionBilling
import com.kape.signup.domain.Credentials
import com.kape.signup.domain.PurchaseSignupRepository
import com.kape.signup.domain.StoreOffer
import com.kape.signup.domain.SubscriptionStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf

class NoInAppSubscriptionStore(
    private val offerInstead: StoreOffer,
) : SubscriptionStore,
    SubscriptionBilling {
    private val _offer = MutableStateFlow<StoreOffer>(StoreOffer.None)
    override val offer: StateFlow<StoreOffer> = _offer.asStateFlow()
    override val purchaseCompleted: Flow<Unit> = emptyFlow()

    override fun hasActiveSubscription(): Flow<Boolean> = flowOf(false)

    override suspend fun hasResumablePurchase(): Boolean = false

    override fun register(activity: Activity) = Unit

    override fun loadPlans(activity: Activity) {
        _offer.value = offerInstead
    }

    override fun purchase(
        planId: String,
        activity: Activity,
    ) = Unit

    override fun reset() = Unit

    override suspend fun initialize() = Unit

    override suspend fun awaitReady(activity: Activity) = Unit

    override fun loadPrices(activity: Activity) = loadPlans(activity)

    override fun isAvailable(): Boolean = false
}

class NoPurchaseSignupRepository : PurchaseSignupRepository {
    override suspend fun signUp(email: String): Credentials? = null
}