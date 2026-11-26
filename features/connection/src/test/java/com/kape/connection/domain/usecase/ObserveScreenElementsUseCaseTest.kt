package com.kape.connection.domain.usecase

import com.kape.customization.data.Element
import com.kape.localprefs.api.CustomizationStore
import com.kape.localprefs.api.SettingsStore
import com.kape.localprefs.data.customization.ScreenElement
import com.kape.settings.data.ObfuscationOptions
import com.kape.settings.data.VpnProtocols
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ObserveScreenElementsUseCaseTest {
    private val protocol = MutableStateFlow(VpnProtocols.OpenVPN)
    private val customizationPrefs =
        mockk<CustomizationStore> {
            every { elements } returns MutableStateFlow(listOf(ScreenElement(Element.ShadowsocksRegionSelection, "shadowsocks")))
        }
    private val settingsPrefs =
        mockk<SettingsStore> {
            every { isShadowsocksObfuscationEnabled } returns MutableStateFlow(true)
            every { selectedObfuscationOption } returns MutableStateFlow(ObfuscationOptions.PIA)
            every { selectedProtocol } returns protocol
        }

    @Test
    fun `the shadowsocks tile follows the obfuscation and protocol settings`() =
        runTest {
            val useCase = ObserveScreenElementsUseCase(customizationPrefs, settingsPrefs)

            assertEquals(
                true,
                useCase()
                    .first()
                    .connection
                    .single()
                    .shouldDisplayElement,
            )

            protocol.value = VpnProtocols.WireGuard

            assertEquals(
                false,
                useCase()
                    .first()
                    .connection
                    .single()
                    .shouldDisplayElement,
            )
        }
}