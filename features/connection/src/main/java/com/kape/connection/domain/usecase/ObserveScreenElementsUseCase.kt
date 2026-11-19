package com.kape.connection.domain.usecase

import com.kape.customization.data.Element
import com.kape.localprefs.api.CustomizationStore
import com.kape.localprefs.api.SettingsStore
import com.kape.localprefs.data.customization.ScreenElement
import com.kape.settings.data.ObfuscationOptions
import com.kape.settings.data.VpnProtocols
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.koin.core.annotation.Factory

data class ScreenElements(
    val connection: List<ScreenElement>,
    val customization: List<ScreenElement>,
)

@Factory
class ObserveScreenElementsUseCase(
    private val customizationPrefs: CustomizationStore,
    private val settingsPrefs: SettingsStore,
) {
    operator fun invoke(): Flow<ScreenElements> =
        combine(
            customizationPrefs.elements,
            settingsPrefs.isShadowsocksObfuscationEnabled,
            settingsPrefs.selectedObfuscationOption,
            settingsPrefs.selectedProtocol,
        ) { elements, obfuscationEnabled, obfuscationOption, protocol ->
            screenElements(elements, isShadowsocksAvailable(obfuscationEnabled, obfuscationOption, protocol))
        }
}

internal fun isShadowsocksAvailable(
    obfuscationEnabled: Boolean,
    obfuscationOption: ObfuscationOptions,
    protocol: VpnProtocols,
): Boolean = obfuscationEnabled && protocol == VpnProtocols.OpenVPN && obfuscationOption == ObfuscationOptions.PIA

internal fun screenElements(
    elements: List<ScreenElement>,
    isShadowsocksAvailable: Boolean,
): ScreenElements {
    fun ScreenElement.isShadowsocks() = element == Element.ShadowsocksRegionSelection
    return ScreenElements(
        connection = elements.map { it.copy(shouldDisplayElement = it.isVisible && (!it.isShadowsocks() || isShadowsocksAvailable)) },
        customization = elements.map { it.copy(shouldDisplayElement = if (it.isShadowsocks()) isShadowsocksAvailable else it.isVisible) },
    )
}