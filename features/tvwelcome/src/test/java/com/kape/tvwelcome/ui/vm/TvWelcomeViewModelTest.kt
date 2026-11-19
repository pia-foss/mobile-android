package com.kape.tvwelcome.ui.vm

import android.app.Activity
import com.kape.contracts.Router
import com.kape.contracts.SubscriptionBilling
import com.kape.data.ComposeDestination
import com.kape.data.TvLoginUsername
import com.kape.data.TvSubscribe
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class TvWelcomeViewModelTest {
    private val router = RecordingRouter()
    private val billing = FakeBilling()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `hides the subscribe button until billing is ready`() {
        val viewModel = TvWelcomeViewModel(router, billing)

        viewModel.checkBillingAvailability(Activity())
        billing.available = true

        assertFalse(viewModel.shouldShowSubscribeButton.value)

        billing.ready.complete(Unit)

        assertTrue(viewModel.shouldShowSubscribeButton.value)
    }

    @Test
    fun `keeps the subscribe button hidden when the store has no billing`() {
        val viewModel = TvWelcomeViewModel(router, billing)

        viewModel.checkBillingAvailability(Activity())
        billing.ready.complete(Unit)

        assertFalse(viewModel.shouldShowSubscribeButton.value)
    }

    @Test
    fun `shows the subscribe button straight away when billing is already available`() {
        billing.available = true

        assertTrue(TvWelcomeViewModel(router, billing).shouldShowSubscribeButton.value)
    }

    @Test
    fun `login and signup navigate to their TV screens`() {
        val viewModel = TvWelcomeViewModel(router, billing)

        viewModel.login()
        viewModel.signup()

        assertEquals(listOf(TvLoginUsername, TvSubscribe), router.destinations)
    }
}

private class FakeBilling : SubscriptionBilling {
    var available = false
    val ready = CompletableDeferred<Unit>()

    override fun hasActiveSubscription(): Flow<Boolean> = flowOf(false)

    override suspend fun initialize() = Unit

    override suspend fun awaitReady(activity: Activity) = ready.await()

    override fun loadPrices(activity: Activity) = Unit

    override fun isAvailable() = available
}

private class RecordingRouter : Router {
    val destinations = mutableListOf<ComposeDestination>()

    override fun updateDestination(destination: ComposeDestination) {
        destinations += destination
    }

    override fun getNavigationState(): StateFlow<ComposeDestination?> = MutableStateFlow(null)

    override fun resetNavigation() = Unit

    override fun navigateBack() = Unit

    override fun getBackState(): StateFlow<Boolean> = MutableStateFlow(false)

    override fun resetBack() = Unit
}