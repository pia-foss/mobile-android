package com.kape.signup.domain.usecase

import android.app.Activity
import com.kape.data.ComposeDestination
import com.kape.data.Subscribe
import com.kape.data.TvWelcome
import com.kape.data.TvWelcomeBack
import com.kape.data.WelcomeBack
import com.kape.signup.domain.Credentials
import com.kape.signup.domain.SharingConsentRepository
import com.kape.signup.domain.StoreOffer
import com.kape.signup.domain.SubscriptionStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class SignupUseCasesTest {
    private val consent = FakeConsent()

    @ParameterizedTest(name = "tv={0} subscribed={1} -> {2}")
    @MethodSource("firstLaunchCases")
    fun `on first launch, consent leads on to the right screen`(
        isTv: Boolean,
        isSubscribed: Boolean,
        expected: ComposeDestination,
    ) = runTest {
        val destination = SubmitConsentUseCase(consent, FakeStore(isSubscribed))(allowSharing = true, isFirstScreen = true, isTv = isTv)

        assertEquals(expected, destination)
        assertEquals(true, consent.allowSharing)
    }

    @Test
    fun `consent asked during signup continues in place`() =
        runTest {
            assertNull(SubmitConsentUseCase(consent, FakeStore(false))(allowSharing = false, isFirstScreen = false, isTv = false))
            assertEquals(false, consent.allowSharing)
        }

    @Test
    fun `after a purchase, asks for consent only if it was never answered`() {
        val step = GetPostPurchaseStepUseCase(consent)
        assertEquals(PostPurchaseStep.Consent, step())

        consent.decided = true
        assertEquals(PostPurchaseStep.Email, step())
    }

    @Test
    fun `signup needs an email`() =
        runTest {
            assertEquals(SignupResult.InvalidEmail, CompleteSignupUseCase { error("not called") }(" "))
        }

    @Test
    fun `signup returns the new account, or fails`() =
        runTest {
            val credentials = Credentials("active", "p1234567", "secret")

            assertEquals(SignupResult.SignedUp(credentials), CompleteSignupUseCase { credentials }("a@b.c"))
            assertEquals(SignupResult.Failed, CompleteSignupUseCase { null }("a@b.c"))
        }

    companion object {
        @JvmStatic
        fun firstLaunchCases() =
            listOf(
                Arguments.of(false, false, Subscribe),
                Arguments.of(false, true, WelcomeBack),
                Arguments.of(true, false, TvWelcome),
                Arguments.of(true, true, TvWelcomeBack),
            )
    }
}

private class FakeConsent : SharingConsentRepository {
    var allowSharing: Boolean? = null
    var decided = false

    override suspend fun setAllowSharing(allow: Boolean) {
        allowSharing = allow
        decided = true
    }

    override fun hasMadeDecision() = decided
}

private class FakeStore(
    private val isSubscribed: Boolean,
) : SubscriptionStore {
    override val offer = MutableStateFlow<StoreOffer>(StoreOffer.None)
    override val purchaseCompleted = emptyFlow<Unit>()

    override fun hasActiveSubscription() = flowOf(isSubscribed)

    override suspend fun hasResumablePurchase() = false

    override fun register(activity: Activity) = Unit

    override fun loadPlans(activity: Activity) = Unit

    override fun purchase(
        planId: String,
        activity: Activity,
    ) = Unit

    override fun reset() = Unit
}