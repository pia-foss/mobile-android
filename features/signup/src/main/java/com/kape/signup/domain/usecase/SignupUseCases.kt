package com.kape.signup.domain.usecase

import com.kape.data.ComposeDestination
import com.kape.data.Subscribe
import com.kape.data.TvWelcome
import com.kape.data.TvWelcomeBack
import com.kape.data.WelcomeBack
import com.kape.signup.domain.Credentials
import com.kape.signup.domain.PurchaseSignupRepository
import com.kape.signup.domain.SharingConsentRepository
import com.kape.signup.domain.SubscriptionStore
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Factory

@Factory
class SubmitConsentUseCase(
    private val consent: SharingConsentRepository,
    private val store: SubscriptionStore,
) {
    suspend operator fun invoke(
        allowSharing: Boolean,
        isFirstScreen: Boolean,
        isTv: Boolean,
    ): ComposeDestination? {
        consent.setAllowSharing(allowSharing)
        if (!isFirstScreen) return null
        val isSubscribed = store.hasActiveSubscription().first()
        return when {
            isTv && isSubscribed -> TvWelcomeBack
            isTv -> TvWelcome
            isSubscribed -> WelcomeBack
            else -> Subscribe
        }
    }
}

enum class PostPurchaseStep { Consent, Email }

@Factory
class GetPostPurchaseStepUseCase(
    private val consent: SharingConsentRepository,
) {
    suspend operator fun invoke(): PostPurchaseStep = if (consent.hasMadeDecision()) PostPurchaseStep.Email else PostPurchaseStep.Consent
}

sealed interface SignupResult {
    data object InvalidEmail : SignupResult

    data object Failed : SignupResult

    data class SignedUp(
        val credentials: Credentials,
    ) : SignupResult
}

@Factory
class CompleteSignupUseCase(
    private val repository: PurchaseSignupRepository,
) {
    suspend operator fun invoke(email: String): SignupResult {
        if (email.isBlank()) return SignupResult.InvalidEmail
        val credentials = repository.signUp(email) ?: return SignupResult.Failed
        return SignupResult.SignedUp(credentials)
    }
}