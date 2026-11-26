package com.kape.splash.domain.usecase

import android.app.Activity
import com.kape.contracts.SubscriptionBilling
import com.kape.data.ComposeDestination
import com.kape.data.Connection
import com.kape.data.Consent
import com.kape.data.Subscribe
import com.kape.data.TvConsent
import com.kape.data.TvWelcome
import com.kape.data.TvWelcomeBack
import com.kape.data.WelcomeBack
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class ResolveSplashDestinationUseCaseTest {
    @ParameterizedTest(name = "loggedIn={0} tv={1} consent={2} subscribed={3} -> {4}")
    @MethodSource("cases")
    fun `routes to the right screen`(
        isLoggedIn: Boolean,
        isTv: Boolean,
        hasConsent: Boolean,
        isSubscribed: Boolean,
        expected: ComposeDestination,
    ) = runTest {
        val useCase = ResolveSplashDestinationUseCase({ hasConsent }, FakeBilling(isSubscribed))

        assertEquals(expected, useCase(isLoggedIn = isLoggedIn, isTv = isTv))
    }

    companion object {
        @JvmStatic
        fun cases() =
            listOf(
                Arguments.of(true, false, false, false, Connection),
                Arguments.of(true, true, false, false, Connection),
                Arguments.of(false, false, false, true, Consent(isFirstScreen = true)),
                Arguments.of(false, true, false, true, TvConsent(isFirstScreen = true)),
                Arguments.of(false, false, true, true, WelcomeBack),
                Arguments.of(false, true, true, true, TvWelcomeBack),
                Arguments.of(false, false, true, false, Subscribe),
                Arguments.of(false, true, true, false, TvWelcome),
            )
    }
}

private class FakeBilling(
    private val isSubscribed: Boolean,
) : SubscriptionBilling {
    override fun hasActiveSubscription() = flowOf(isSubscribed)

    override suspend fun initialize() = Unit

    override suspend fun awaitReady(activity: Activity) = Unit

    override fun loadPrices(activity: Activity) = Unit

    override fun isAvailable() = false
}