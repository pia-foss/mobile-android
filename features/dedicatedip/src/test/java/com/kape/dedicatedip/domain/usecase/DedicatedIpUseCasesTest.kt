package com.kape.dedicatedip.domain.usecase

import android.app.Activity
import com.kape.contracts.AppInfo
import com.kape.contracts.ConnectionManager
import com.kape.data.model.DipPurchaseData
import com.kape.data.vpnserver.VpnServer
import com.kape.dedicatedip.domain.DedicatedIpRepository
import com.kape.dedicatedip.domain.DipActivationResult
import com.kape.dedicatedip.domain.DipCountry
import com.kape.dedicatedip.domain.DipMonthlyPlan
import com.kape.dedicatedip.domain.DipPurchaseStore
import com.kape.dedicatedip.domain.DipSignupStateRepository
import com.kape.dedicatedip.domain.DipYearlyPlan
import com.kape.dedicatedip.domain.ServerSelectionRepository
import com.kape.dip.data.DedicatedIpSelectedCountry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class DedicatedIpUseCasesTest {
    private val events = mutableListOf<String>()
    private val repository = FakeRepository(events)
    private val selection = FakeSelection(events)
    private val signupState = FakeSignupState()
    private val vpn =
        object : ConnectionManager {
            override fun isConnected() = true

            override suspend fun connect(
                server: VpnServer,
                isManual: Boolean,
                stopCallback: () -> Unit,
                showDialog: () -> Unit,
            ) = Unit

            override suspend fun connectToLastKnownOrOptimalServer() = Unit

            override suspend fun reconnect(
                server: VpnServer,
                stopCallback: () -> Unit,
            ) = Unit

            override fun isConnectionInProgress() = false

            override suspend fun disconnect() {
                events += "disconnect"
            }
        }

    @Test
    fun `activating forgets a bought token once it's active`() =
        runTest {
            signupState.purchasedToken.value = "bought"
            repository.activation = DipActivationResult.Active

            assertEquals(DipActivationResult.Active, ActivateDedicatedIpUseCase(repository, signupState)("bought"))
            assertEquals("", signupState.purchasedToken.value)
        }

    @Test
    fun `a failed activation keeps the bought token`() =
        runTest {
            signupState.purchasedToken.value = "bought"
            repository.activation = DipActivationResult.Invalid

            ActivateDedicatedIpUseCase(repository, signupState)("bought")

            assertEquals("bought", signupState.purchasedToken.value)
        }

    @Test
    fun `removing the selected Dedicated IP clears the selection and disconnects`() =
        runTest {
            selection.selected = "dip-region"

            RemoveDedicatedIpUseCase(repository, selection, vpn)("dip-region", "token")

            assertEquals(listOf("remove token", "unquick dip-region", "clear selection", "disconnect"), events)
        }

    @Test
    fun `removing another Dedicated IP leaves the connection alone`() =
        runTest {
            selection.selected = "other-region"

            RemoveDedicatedIpUseCase(repository, selection, vpn)("dip-region", "token")

            assertEquals(listOf("remove token", "unquick dip-region"), events)
        }

    @Test
    fun `removing a Dedicated IP the user doesn't have does nothing more`() =
        runTest {
            repository.has = false

            RemoveDedicatedIpUseCase(repository, selection, vpn)("dip-region", "token")

            assertEquals(listOf("remove token"), events)
        }

    @Test
    fun `fetches the token for the chosen country and keeps it`() =
        runTest {
            signupState.country = DedicatedIpSelectedCountry(countryCode = "DE", countryName = "Germany", regionName = "Berlin")
            val store = FakeStore(token = Result.success("new-token"))

            assertEquals(TokenFetchResult.Fetched, FetchPurchasedDipTokenUseCase(store, signupState)())
            assertEquals("DE/Berlin", store.fetchedFor)
            assertEquals("new-token", signupState.purchasedToken.value)
        }

    @Test
    fun `can't fetch a token without a chosen country`() =
        runTest {
            assertEquals(TokenFetchResult.NoCountrySelected, FetchPurchasedDipTokenUseCase(FakeStore(), signupState)())
        }

    @Test
    fun `signup is only offered on Google Play builds`() =
        runTest {
            assertEquals(true, IsDipSignupAvailableUseCase(appInfo("google"), signupState)().first())
            assertEquals(false, IsDipSignupAvailableUseCase(appInfo("fdroid"), signupState)().first())
        }

    private fun appInfo(flavor: String) =
        object : AppInfo {
            override val buildFlavor = flavor
            override val buildType = "debug"
            override val versionName = "1.0"
            override val versionCode = 1
            override val isFreshInstall = false
        }
}

private class FakeRepository(
    private val events: MutableList<String>,
) : DedicatedIpRepository {
    var activation: DipActivationResult = DipActivationResult.Active
    var has = true

    override suspend fun activate(token: String) = activation

    override suspend fun remove(dipToken: String): Boolean {
        events += "remove $dipToken"
        return has
    }
}

private class FakeSelection(
    private val events: MutableList<String>,
) : ServerSelectionRepository {
    var selected: String? = null

    override suspend fun selectedServerKey() = selected

    override suspend fun clearSelectedServer() {
        events += "clear selection"
    }

    override suspend fun removeFromQuickConnect(serverKey: String) {
        events += "unquick $serverKey"
    }
}

private class FakeSignupState : DipSignupStateRepository {
    var country: DedicatedIpSelectedCountry? = null
    override val selectedCountry = MutableStateFlow<DedicatedIpSelectedCountry?>(null)

    override suspend fun selectedCountryNow() = country

    override suspend fun selectCountry(country: DedicatedIpSelectedCountry) {
        this.country = country
    }

    override val selectedProductId = MutableStateFlow("")

    override suspend fun selectProduct(productId: String) {
        selectedProductId.value = productId
    }

    override val purchasedToken = MutableStateFlow("")

    override suspend fun setPurchasedToken(token: String) {
        purchasedToken.value = token
    }

    override suspend fun clearPurchasedToken() {
        purchasedToken.value = ""
    }

    override fun isSignupEnabled(isGooglePlayBuild: Boolean) = flowOf(isGooglePlayBuild)
}

private class FakeStore(
    private val token: Result<String> = Result.failure(IllegalStateException()),
) : DipPurchaseStore {
    var fetchedFor: String? = null

    override suspend fun supportedCountries(): List<DipCountry>? = null

    override suspend fun monthlyPlan(): DipMonthlyPlan? = null

    override suspend fun yearlyPlan(): DipYearlyPlan? = null

    override suspend fun purchase(
        activity: Activity,
        productId: String,
    ): Result<DipPurchaseData> = Result.failure(IllegalStateException())

    override suspend fun unregisteredPurchases(productIds: List<String>): List<String>? = null

    override suspend fun registerPurchase(purchase: DipPurchaseData?): Result<Unit> = Result.success(Unit)

    override suspend fun fetchToken(
        countryCode: String,
        regionName: String,
    ): Result<String> {
        fetchedFor = "$countryCode/$regionName"
        return token
    }

    override fun hasActiveVpnSubscription() = flowOf(false)

    override suspend fun renew(dipToken: String) = false
}