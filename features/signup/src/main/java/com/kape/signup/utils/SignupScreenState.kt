package com.kape.signup.utils

import androidx.compose.runtime.MutableState
import com.kape.signup.data.models.Credentials

data class SignupScreenState(
    val loading: Boolean,
    val step: SignupStep,
    val error: SignupError? = null,
)

val DEFAULT = SignupScreenState(loading = false, SignupStep.Default)
val LOADING = SignupScreenState(loading = true, SignupStep.LoadingPlans)
val CONSENT = SignupScreenState(loading = false, SignupStep.Consent)
val EMAIL = SignupScreenState(loading = false, SignupStep.Email)
val IN_PROCESS = SignupScreenState(loading = false, SignupStep.InProcess)
val ERROR_EMAIL_INVALID = SignupScreenState(
    loading = false,
    SignupStep.Default,
    error = SignupError.EmailInvalid,
)
val ERROR_REGISTRATION = SignupScreenState(
    loading = false,
    SignupStep.Default,
    error = SignupError.RegistrationFailed,
)
val SUBSCRIPTIONS = SignupScreenState(loading = false, SignupStep.Subscriptions(true))
val SUBSCRIPTIONS_FAILED_TO_LOAD = SignupScreenState(
    loading = false,
    SignupStep.Subscriptions(supportsSubscription = true, displaySubscribeButton = false),
)
val NO_IN_APP_SUBSCRIPTIONS = SignupScreenState(loading = false, SignupStep.Subscriptions(false))
val META_SUBSCRIPTIONS = SignupScreenState(loading = false, SignupStep.Subscriptions(false))
val AMAZON_LOGIN = SignupScreenState(
    loading = false,
    SignupStep.Subscriptions(supportsSubscription = true, displaySubscribeButton = false),
)

fun signedUp(credentials: Credentials) =
    SignupScreenState(loading = false, SignupStep.SignedUp(credentials))

sealed class SignupStep {
    data object Default : SignupStep()
    data class Subscriptions(
        val supportsSubscription: Boolean,
        val displaySubscribeButton: Boolean = true,
    ) : SignupStep()

    data object Consent : SignupStep()
    data object Email : SignupStep()
    data object InProcess : SignupStep()
    data object LoadingPlans : SignupStep()
    data class SignedUp(val credentials: Credentials) : SignupStep()
}

sealed class SignupError {
    data object EmailInvalid : SignupError()
    data object RegistrationFailed : SignupError()
}

data class SubscriptionData(val selected: MutableState<Plan>, val yearly: Plan, val monthly: Plan)

data class Plan(
    val id: String,
    val period: String,
    val hasFreeTrial: Boolean,
    val mainPrice: String,
    val secondaryPrice: String? = null,
)