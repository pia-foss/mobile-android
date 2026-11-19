package com.kape.automation.ui.vm

import com.kape.automation.domain.AutomationServiceControl
import com.kape.automation.domain.EditNetworkRulesUseCase
import com.kape.automation.domain.EnableAutomationUseCase
import com.kape.automation.domain.NetworkObserver
import com.kape.automation.domain.NetworkRulesRepository
import com.kape.automation.domain.ObserveAutomationEnabledUseCase
import com.kape.automation.domain.ObserveNetworkRulesUseCase
import com.kape.automation.domain.ToggleAutomationUseCase
import com.kape.contracts.LocationPermissionStatus
import com.kape.contracts.Router
import com.kape.data.AutomationBackgroundLocation
import com.kape.data.AutomationLocation
import com.kape.localprefs.api.SettingsStore
import com.kape.networkmanagement.data.NetworkBehavior
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class AutomationViewModelTest {
    private var enabled = false
    private var fineLocation = true
    private var backgroundLocation = true
    private val router = mockk<Router>(relaxed = true)
    private val settingsPrefs =
        mockk<SettingsStore>(relaxed = true) {
            every { isAutomationEnabled } returns MutableStateFlow(false)
            coEvery { isAutomationEnabledNow() } answers { enabled }
        }
    private val monitor = mockk<NetworkObserver>(relaxed = true) { every { ssid } returns MutableStateFlow(null) }
    private val service = mockk<AutomationServiceControl>(relaxed = true)
    private val rules = mockk<NetworkRulesRepository>(relaxed = true) { every { getRules() } returns emptyFlow() }
    private val permissions =
        mockk<LocationPermissionStatus> {
            every { isFineLocationPermissionGranted() } answers { fineLocation }
            every { isBackgroundLocationPermissionGranted() } answers { backgroundLocation }
        }
    private lateinit var viewModel: AutomationViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        val enable = EnableAutomationUseCase(settingsPrefs, service, monitor)
        viewModel =
            AutomationViewModel(
                router = router,
                locationPermissions = permissions,
                networkObserver = monitor,
                enableAutomation = enable,
                toggleAutomation = ToggleAutomationUseCase(settingsPrefs, service, permissions, enable),
                editRules = EditNetworkRulesUseCase(rules, monitor),
                observeAutomationEnabled = ObserveAutomationEnabledUseCase(settingsPrefs),
                observeRules = ObserveNetworkRulesUseCase(rules),
            )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `turning automation on starts the service and applies the rules`() {
        viewModel.onAutomationToggled()

        coVerifyOrder {
            settingsPrefs.setAutomationEnabled(true)
            service.start()
            monitor.reapplyRules()
        }
    }

    @Test
    fun `turning automation off stops the service`() {
        enabled = true

        viewModel.onAutomationToggled()

        coVerify { settingsPrefs.setAutomationEnabled(false) }
        verify { service.stop() }
    }

    @Test
    fun `without location access, turning it on asks for the missing permission`() {
        fineLocation = false
        viewModel.onAutomationToggled()
        verify { router.updateDestination(AutomationLocation) }

        fineLocation = true
        backgroundLocation = false
        viewModel.onAutomationToggled()
        verify { router.updateDestination(AutomationBackgroundLocation) }

        verify(exactly = 0) { service.start() }
    }

    @Test
    fun `changing a rule applies it to the current network once saved`() {
        viewModel.addRule("\"Cafe\"", NetworkBehavior.AlwaysConnect)

        coVerifyOrder {
            rules.addRule("\"Cafe\"", NetworkBehavior.AlwaysConnect)
            monitor.reapplyRules()
        }
    }
}