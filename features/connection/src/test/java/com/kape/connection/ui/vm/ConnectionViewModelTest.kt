package com.kape.connection.ui.vm

import com.kape.connection.domain.usecase.ConnectOnLaunchUseCase
import com.kape.connection.domain.usecase.ConnectUseCase
import com.kape.connection.domain.usecase.DisconnectUseCase
import com.kape.connection.domain.usecase.DisplayedServer
import com.kape.connection.domain.usecase.ObserveDisplayedServerUseCase
import com.kape.connection.domain.usecase.ObserveScreenElementsUseCase
import com.kape.connection.domain.usecase.ObserveShortcutRequestsUseCase
import com.kape.connection.domain.usecase.ShortcutRequest
import com.kape.connection.vpnServer
import com.kape.contracts.ConnectionInfoProvider
import com.kape.contracts.DedicatedIpHome
import com.kape.contracts.PermissionStatus
import com.kape.contracts.Router
import com.kape.data.Settings
import com.kape.data.VpnPermission
import com.kape.data.VpnRegionSelection
import com.kape.rating.data.RatingDialogType
import com.kape.rating.utils.RatingTool
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class ConnectionViewModelTest {
    private val de = vpnServer("de")
    private var vpnAllowed = true
    private var inConnectState = false
    private val shortcuts = MutableSharedFlow<ShortcutRequest>(extraBufferCapacity = 4)
    private val ratingPrompts = MutableStateFlow<RatingDialogType?>(null)

    private val router = mockk<Router>(relaxed = true)
    private val connect = mockk<ConnectUseCase> { every { this@mockk(any(), any(), any()) } returns Job() }
    private val disconnect = mockk<DisconnectUseCase> { every { this@mockk() } returns Job() }
    private val ratingTool = mockk<RatingTool>(relaxed = true) { every { showRating } returns ratingPrompts }
    private val connectionInfo =
        mockk<ConnectionInfoProvider>(relaxed = true) {
            every { isInConnectState() } answers { inConnectState }
            every { isConnected() } answers { inConnectState }
        }

    private lateinit var viewModel: ConnectionViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel =
            ConnectionViewModel(
                router = router,
                permissions = mockk<PermissionStatus> { every { isVpnProfileInstalled() } answers { vpnAllowed } },
                connectionInfo = connectionInfo,
                connect = connect,
                disconnect = disconnect,
                quickConnect = mockk(relaxed = true),
                switchToAutomaticProtocol = mockk(relaxed = true),
                snooze = mockk(relaxed = true),
                ratingTool = ratingTool,
                problemDetector = mockk(relaxed = true) { every { showNudge } returns MutableStateFlow(false) },
                dedicatedIpHome = mockk<DedicatedIpHome>(relaxed = true) { every { observeSignupBannerVisible() } returns flowOf(false) },
                appUpdateStatus = mockk(relaxed = true) { every { hasUpdateAvailable } returns MutableStateFlow(false) },
                paymentIssueChecker = mockk(relaxed = true),
                observeDisplayedServer =
                    mockk<ObserveDisplayedServerUseCase> {
                        every { this@mockk() } returns
                            flowOf(DisplayedServer(de, isOptimal = false, showOptimalLocationInfo = false))
                    },
                observeScreenElements = mockk<ObserveScreenElementsUseCase> { every { this@mockk() } returns emptyFlow() },
                connectOnLaunch = mockk<ConnectOnLaunchUseCase>(relaxed = true),
                observeShortcutRequests = mockk<ObserveShortcutRequestsUseCase> { every { this@mockk() } returns shortcuts },
                networkStatus = mockk(relaxed = true) { every { isOnline } returns MutableStateFlow(true) },
            )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `the button connects to the displayed server`() {
        viewModel.onConnectionButtonClicked()

        verify { connect(de, true, any()) }
    }

    @Test
    fun `the button disconnects while connected or connecting`() {
        inConnectState = true

        viewModel.onConnectionButtonClicked()

        verify { disconnect() }
        verify(exactly = 0) { connect(any(), any(), any()) }
    }

    @Test
    fun `without VPN permission, the button asks for it`() {
        vpnAllowed = false

        viewModel.onConnectionButtonClicked()

        verify { router.updateDestination(VpnPermission) }
        verify(exactly = 0) { connect(any(), any(), any()) }
    }

    @Test
    fun `the change-server and settings shortcuts navigate`() {
        shortcuts.tryEmit(ShortcutRequest.ChangeServer)
        shortcuts.tryEmit(ShortcutRequest.OpenSettings)

        verify { router.updateDestination(VpnRegionSelection) }
        verify { router.updateDestination(Settings) }
    }

    @Test
    fun `the connect shortcut connects unless already connected`() {
        shortcuts.tryEmit(ShortcutRequest.Connect)

        verify(exactly = 1) { connect(de, true, any()) }
    }

    @Test
    fun `a thumbs down asks for feedback, and answering it asks again later`() {
        ratingPrompts.value = RatingDialogType.General

        viewModel.onRatingThumbsDown()
        assertEquals(RatingDialogType.Feedback, viewModel.ratingPrompt.value)

        viewModel.onFeedbackFinished()
        assertNull(viewModel.ratingPrompt.value)
        coVerify { ratingTool.updateRatingDate() }
    }
}