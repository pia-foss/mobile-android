package com.kape.customization

import com.kape.contracts.ScreenElementProvider
import com.kape.customization.data.Element
import com.kape.data.DI
import com.kape.localprefs.data.customization.ScreenElement
import com.kape.localprefs.prefs.CustomizationPrefs
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.settings.data.ObfuscationOptions
import com.kape.settings.data.VpnProtocols
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.Named

class ScreenElementProviderImpl(
    private val customizationPrefs: CustomizationPrefs,
    private val settingsPrefs: SettingsPrefs,
    @Named(DI.IO_SCOPE) private val ioScope: CoroutineScope,
) : ScreenElementProvider {
    private val _screenElements = MutableStateFlow(customizationPrefs.elements.value)
    override val screenElements: StateFlow<List<ScreenElement>> = _screenElements.asStateFlow()

    private val _customizationElements = MutableStateFlow(customizationPrefs.elements.value)
    override val customizationElements: StateFlow<List<ScreenElement>> =
        _customizationElements.asStateFlow()

    init {
        ioScope.launch {
            combine(
                customizationPrefs.elements,
                settingsPrefs.isShadowsocksObfuscationEnabled,
                settingsPrefs.selectedObfuscationOption,
                settingsPrefs.selectedProtocol,
            ) { elements, obfuscationEnabled, obfuscationOption, selectedProtocol ->
                val shadowsocksAvailable =
                    obfuscationEnabled &&
                        selectedProtocol == VpnProtocols.OpenVPN &&
                        obfuscationOption == ObfuscationOptions.PIA

                val connectionList =
                    elements.map { el ->
                        el.copy(
                            shouldDisplayElement =
                                if (el.element == Element.ShadowsocksRegionSelection) {
                                    el.isVisible && shadowsocksAvailable
                                } else {
                                    el.isVisible
                                },
                        )
                    }
                val customizationList =
                    elements.map { el ->
                        el.copy(
                            shouldDisplayElement =
                                if (el.element == Element.ShadowsocksRegionSelection) {
                                    shadowsocksAvailable
                                } else {
                                    el.isVisible
                                },
                        )
                    }
                Pair(connectionList, customizationList)
            }.collectLatest { (connectionList, customizationList) ->
                _screenElements.update { connectionList }
                _customizationElements.update { customizationList }
            }
        }
    }
}