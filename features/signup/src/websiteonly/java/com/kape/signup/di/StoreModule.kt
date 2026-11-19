package com.kape.signup.di

import com.kape.contracts.SubscriptionBilling
import com.kape.signup.data.NoInAppSubscriptionStore
import com.kape.signup.data.NoPurchaseSignupRepository
import com.kape.signup.domain.PurchaseSignupRepository
import com.kape.signup.domain.StoreOffer
import com.kape.signup.domain.SubscriptionStore
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module
class StoreModule {
    @Singleton(binds = [SubscriptionStore::class, SubscriptionBilling::class])
    fun provideStore(): NoInAppSubscriptionStore = NoInAppSubscriptionStore(offerInstead = StoreOffer.WebsiteOnly)

    @Singleton(binds = [PurchaseSignupRepository::class])
    fun provideSignupRepository(): NoPurchaseSignupRepository = NoPurchaseSignupRepository()
}