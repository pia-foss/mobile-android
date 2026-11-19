package com.kape.splash.domain.usecase

import com.kape.contracts.SubscriptionBilling
import com.kape.data.ComposeDestination
import com.kape.data.Connection
import com.kape.data.Consent
import com.kape.data.Subscribe
import com.kape.data.TvConsent
import com.kape.data.TvWelcome
import com.kape.data.TvWelcomeBack
import com.kape.data.WelcomeBack
import com.kape.splash.domain.ConsentRepository
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Factory

@Factory
class ResolveSplashDestinationUseCase(
    private val consentRepository: ConsentRepository,
    private val subscriptionBilling: SubscriptionBilling,
) {
    suspend operator fun invoke(
        isLoggedIn: Boolean,
        isTv: Boolean,
    ): ComposeDestination =
        when {
            isLoggedIn -> Connection
            !consentRepository.hasMadeConsentDecision() -> if (isTv) TvConsent(isFirstScreen = true) else Consent(isFirstScreen = true)
            subscriptionBilling.hasActiveSubscription().first() -> if (isTv) TvWelcomeBack else WelcomeBack
            else -> if (isTv) TvWelcome else Subscribe
        }
}