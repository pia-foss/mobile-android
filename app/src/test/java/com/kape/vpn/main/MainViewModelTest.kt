package com.kape.vpn.main

import com.kape.account.domain.AuthenticationRepository
import com.kape.account.domain.LoginResult
import com.kape.account.domain.usecase.GetPostLoginDestinationUseCase
import com.kape.contracts.PermissionStatus
import com.kape.contracts.Router
import com.kape.data.ComposeDestination
import com.kape.data.Connection
import com.kape.login.domain.usecase.LoginWithLinkUseCase
import com.kape.vpn.automation.StartAutomationIfEnabledUseCase
import com.kape.vpn.shortcuts.RecordShortcutActionUseCase
import com.kape.vpn.shortcuts.ShortcutAction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class MainViewModelTest {
    private val router = FakeRouter()
    private val recordedShortcuts = mutableListOf<ShortcutAction>()
    private var automationStarts = 0

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(automationEnabled: Boolean = false) =
        MainViewModel(
            router = router,
            loginWithLink = LoginWithLinkUseCase(LoggedOut, GetPostLoginDestinationUseCase(AllPermissions)),
            recordShortcutAction = RecordShortcutActionUseCase { recordedShortcuts += it },
            startAutomationIfEnabled = StartAutomationIfEnabledUseCase({ automationEnabled }, { automationStarts++ }),
        )

    @Test
    fun `starts automation once when the app opens`() {
        viewModel(automationEnabled = true)

        assertEquals(1, automationStarts)
    }

    @Test
    fun `records the shortcut that opened the app`() {
        viewModel().onIntent(ShortcutAction.Connect.intentAction, data = null)

        assertEquals(listOf(ShortcutAction.Connect), recordedShortcuts)
    }

    @Test
    fun `logs in with a login link and moves on`() {
        viewModel().onIntent(action = "android.intent.action.VIEW", data = "piavpn:login?token=abc")

        assertEquals(listOf<ComposeDestination>(Connection), router.destinations)
    }

    @Test
    fun `ignores a launcher intent`() {
        viewModel().onIntent(action = "android.intent.action.MAIN", data = null)

        assertEquals(emptyList<ShortcutAction>(), recordedShortcuts)
        assertEquals(emptyList<ComposeDestination>(), router.destinations)
    }
}

private class FakeRouter : Router {
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

private object LoggedOut : AuthenticationRepository {
    override suspend fun isLoggedIn() = false

    override suspend fun loginWithCredentials(
        username: String,
        password: String,
    ) = LoginResult.Success

    override suspend fun requestLoginLink(email: String) = LoginResult.Success

    override suspend fun loginWithReceipt(
        purchaseToken: String,
        productId: String,
        packageName: String,
    ) = LoginResult.Success

    override suspend fun loginWithToken(apiToken: String) = LoginResult.Success

    override suspend fun logout() = true
}

private object AllPermissions : PermissionStatus {
    override fun isVpnProfileInstalled() = true

    override fun areNotificationsAllowed() = true
}