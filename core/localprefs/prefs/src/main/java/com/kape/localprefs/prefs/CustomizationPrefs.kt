package com.kape.localprefs.prefs

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.kape.customization.data.Element
import com.kape.localprefs.Prefs
import com.kape.localprefs.data.customization.ScreenElement
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Singleton

private val ORDERED_ELEMENTS = stringPreferencesKey("ordered-elements")
private val ORDERED_ELEMENTS_V2 = stringPreferencesKey("ordered-elements-v2")

@Singleton
class CustomizationPrefs(
    context: Context,
) : Prefs(context, "customization") {
    val elements: StateFlow<List<ScreenElement>> =
        getElements().stateIn(scope, SharingStarted.WhileSubscribed(waitTime), defaultList())

    @Deprecated("Deprecated in favor of setElements()")
    suspend fun setOrderedElements(orderedElements: List<ScreenElement>) {
        dataStore.edit { it[ORDERED_ELEMENTS] = Json.encodeToString(orderedElements) }
    }

    suspend fun setElements(orderedElements: List<ScreenElement>) {
        dataStore.edit { it[ORDERED_ELEMENTS_V2] = Json.encodeToString(orderedElements) }
    }

    private fun getElements(): Flow<List<ScreenElement>> =
        dataStore.data.map { prefs ->
            prefs[ORDERED_ELEMENTS_V2]?.let { Json.decodeFromString(it) }
                ?: prefs[ORDERED_ELEMENTS]?.let {
                    Json.decodeFromString<List<ScreenElement>>(it).takeIf { list -> list.isNotEmpty() }
                }
                ?: defaultList()
        }

    private fun defaultList(): List<ScreenElement> =
        listOf(
            ScreenElement(Element.VpnRegionSelection, "vpn-region-selection"),
            ScreenElement(
                Element.ShadowsocksRegionSelection,
                "shadowsocks-region-selection",
                shouldDisplayElement = false,
            ),
            ScreenElement(Element.IpInfo, "ip-info"),
            ScreenElement(Element.QuickConnect, "quick-connect"),
            ScreenElement(Element.QuickSettings, name = "quick-settings"),
            ScreenElement(Element.Snooze, name = "snooze"),
            ScreenElement(Element.Traffic, isVisible = false, name = "traffic"),
            ScreenElement(Element.ConnectionInfo, isVisible = false, name = "conection-info"),
        )
}